package org.example.infrastructure.controller;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import org.example.domain.generator.LevelGenerator;
import org.example.domain.model.entity.GameSession;
import org.example.domain.model.entity.Level;
import org.example.domain.model.entity.Player;
import org.example.domain.model.entity.Position;
import org.example.domain.model.enums.GameState;
import org.example.domain.service.*;
import org.example.infrastructure.data.repository.StatisticsRepository;
import org.example.infrastructure.presentation.screen.MenuScreen;
import org.example.infrastructure.presentation.screen.ScoresMenuScreen;
import org.example.infrastructure.presentation.screen.StartMenuScreen;

public class MenuController {
    private final MenuScreen menuScreen = new StartMenuScreen();

    // Сервисы, которые понадобятся для игры
    private final StatisticsService statisticsService = new StatisticsService();
    private final DifficultyBalanceService difficultyBalanceService;
    private final GameSessionService gameSessionService;
    private final ItemService itemService;
    private final MonsterAiService monsterAiService;
    private final MovementService movementService;
    private final CombatService combatService;
    private final LevelService levelService;
    private final VisibilityService visibilityService;

    private final StatisticsRepository statisticsRepository;
    private final LevelGenerator levelGenerator = new LevelGenerator();

    private static final int START_GAME_OPTION = 0;
    private static final int CONTINUE_GAME_OPTION = 1;
    private static final int SCORES_OPTION = 2;
    private static final int EXIT_OPTION = 3;
    private static final int MESSAGE_SHOW_TIME = 1500; // 1.5 секунды

    public MenuController() {
        // Создаём сервис балансировки сложности
        this.difficultyBalanceService = new DifficultyBalanceService();

        // Создаём сервисы с внедрением DifficultyBalanceService
        this.combatService = new CombatService(difficultyBalanceService);
        this.levelService = new LevelService();
        this.itemService = new ItemService(statisticsService, difficultyBalanceService);
        this.movementService =
                new MovementService(
                        combatService, itemService, levelService, statisticsService, difficultyBalanceService);
        this.monsterAiService = new MonsterAiService(movementService, combatService, levelService);
        this.statisticsRepository = new StatisticsRepository();
        this.gameSessionService =
                new GameSessionService(
                        levelGenerator, statisticsService, statisticsRepository, difficultyBalanceService);
        this.visibilityService = new VisibilityService(levelService);
    }

    public void runMenu(Screen screen) throws Exception {
        boolean running = true;

        while (running) {
            menuScreen.drawMenu(screen);

            KeyStroke key = screen.readInput();

            if (key != null) {
                switch (key.getKeyType()) {
                    case ArrowUp -> menuScreen.moveUp();
                    case ArrowDown -> menuScreen.moveDown();
                    case Enter -> {
                        int choice = menuScreen.getSelectedOption();

                        if (choice == START_GAME_OPTION) {
                            startGame(screen);
                        } else if (choice == CONTINUE_GAME_OPTION) {
                            if (gameSessionService.hasSave()) {
                                continueGame(screen);
                            } else {
                                showNoSaveMessage(screen);
                            }
                        } else if (choice == SCORES_OPTION) {
                            ScoresMenuScreen scoresMenu = new ScoresMenuScreen(statisticsRepository);
                            scoresMenu.show(screen);
                        } else if (choice == EXIT_OPTION) {
                            running = false;
                        }
                    }
                }
            }
        }
    }

    private void startGame(Screen screen) throws Exception {
        // Создаём новую игровую сессию
        GameSession gameSession = gameSessionService.createNewGame();

        // Создаём GameController с сессией и сервисами
        GameController gameController =
                new GameController(
                        gameSession,
                        gameSessionService,
                        itemService,
                        monsterAiService,
                        movementService,
                        visibilityService,
                        statisticsService,
                        levelService);

        // Запускаем игру
        gameController.gameLoop(screen);
    }

    private void continueGame(Screen screen) throws Exception {
        screen.clear();
        screen.refresh();

        // Загружаем сохранённую сессию
        GameSession gameSession = gameSessionService.loadGame();

        if (gameSession != null) {
            gameSession.setGameState(GameState.PLAYING);

            Player player = gameSession.getPlayer();
            Level level = gameSession.getCurrentLevel();
            visibilityService.initializeLevel(level, new Position(player.getX(), player.getY()));

            // Создаём GameController с загруженной сессией
            GameController gameController =
                    new GameController(
                            gameSession,
                            gameSessionService,
                            itemService,
                            monsterAiService,
                            movementService,
                            visibilityService,
                            statisticsService,
                            levelService);

            // Запускаем игру
            gameController.gameLoop(screen);
        }
    }

    private void showNoSaveMessage(Screen screen) throws Exception {
        screen.clear();
        String message = "Нет доступных сохранений!";
        for (int i = 0; i < message.length(); i++) {
            screen.setCharacter(
                    10 + i,
                    10,
                    new com.googlecode.lanterna.TextCharacter(
                            message.charAt(i),
                            com.googlecode.lanterna.TextColor.ANSI.YELLOW,
                            com.googlecode.lanterna.TextColor.ANSI.BLACK));
        }
        screen.refresh();
        Thread.sleep(MESSAGE_SHOW_TIME);
    }
}
