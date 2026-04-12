// GameController.java
package org.example.infrastructure.controller;

import static org.example.infrastructure.controller.SelectionState.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.util.ArrayList;
import java.util.List;
import org.example.domain.generator.LevelGenerator;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.Direction;
import org.example.domain.model.enums.GameState;
import org.example.domain.model.enums.KeyColor;
import org.example.domain.model.enums.MoveResultType;
import org.example.domain.service.*;
import org.example.infrastructure.data.repository.StatisticsRepository;
import org.example.infrastructure.presentation.screen.DeathMenuScreen;
import org.example.infrastructure.presentation.screen.PlayScreen;
import org.example.infrastructure.presentation.screen.ScoresMenuScreen;

public class GameController {

    private final PlayScreen playScreen = new PlayScreen();

    private final GameSession gameSession;

    private final GameSessionService gameSessionService;
    private final LevelService levelService;
    private final ItemService itemService;
    private final MonsterAiService monsterAiService;
    private final MovementService movementService;
    private final VisibilityService visibilityService;
    private final StatisticsService statisticsService;
    private final DifficultyBalanceService difficultyBalanceService;
    private final DoorService doorService;

    private final StatisticsRepository statisticsRepository;

    private boolean playerActed = false;
    private boolean fogOfWarEnabled = true;
    private static final int MAX_NOTIFICATIONS = 5;

    private SelectionState selectionState = SelectionState.NONE;
    private final List<String> notifications = new ArrayList<>();

    public GameController(
            GameSession gameSession,
            GameSessionService gameSessionService,
            ItemService itemService,
            MonsterAiService monsterAiService,
            MovementService movementService,
            VisibilityService visibilityService,
            StatisticsService statisticsService,
            LevelService levelService) {
        this.gameSession = gameSession;
        this.gameSessionService = gameSessionService;
        this.itemService = itemService;
        this.monsterAiService = monsterAiService;
        this.movementService = movementService;
        this.visibilityService = visibilityService;
        this.statisticsService = statisticsService;
        this.statisticsRepository = new StatisticsRepository();
        this.difficultyBalanceService = gameSessionService.getDifficultyBalanceService();
        this.doorService = new DoorService();
        this.levelService = levelService;

        if (movementService != null) {
            movementService.setCurrentSessionId(gameSession.getSessionId());
        }
        if (itemService != null && itemService.getDifficultyBalanceService() == null) {
            itemService.setDifficultyBalanceService(difficultyBalanceService);
        }
    }

    public void gameLoop(Screen screen) throws Exception {
        Player player = gameSession.getPlayer();
        Level level = gameSession.getCurrentLevel();
        visibilityService.initializeLevel(level, new Position(player.getX(), player.getY()));

        while (gameSession.isActive()) {
            player = gameSession.getPlayer();
            level = gameSession.getCurrentLevel();

            if (visibilityService.isFogOfWarEnabled()) {
                visibilityService.calculateFov(level, new Position(player.getX(), player.getY()));
            }

            playScreen.draw(
                    screen,
                    level,
                    player,
                    player.getBackpack(),
                    notifications,
                    selectionState,
                    visibilityService);

            KeyStroke key = screen.readInput();
            handleInput(key);

            if (playerActed && selectionState == SelectionState.NONE) {
                player.incrementTurn();
                if (visibilityService.isFogOfWarEnabled()) {
                    visibilityService.calculateFov(level, new Position(player.getX(), player.getY()));
                }
            }

            if (gameSession.shouldProcessMonsters() && selectionState == SelectionState.NONE) {
                monsterAiService.processAllMonsters(level, player);

                player = gameSession.getPlayer();
                level = gameSession.getCurrentLevel();

                visibilityService.calculateFov(level, new Position(player.getX(), player.getY()));
            }
            gameSessionService.checkGameState(gameSession);

            if (!player.isAlive()) {
                gameSession.gameOver();
                showDeathMenu(screen);
                break;
            }

            if (gameSession.isVictory()) {
                showVictoryMenu(screen);
                break;
            }
        }
    }

    public void toggleFogOfWar() {
        this.fogOfWarEnabled = !this.fogOfWarEnabled;
        visibilityService.setFogOfWarEnabled(this.fogOfWarEnabled);

        Level currentLevel = gameSession.getCurrentLevel();
        Player player = gameSession.getPlayer();

        if (this.fogOfWarEnabled) {
            // Включаем туман: восстанавливаем сохраненное состояние видимости
            visibilityService.restoreVisibilityState(currentLevel);
            // Восстанавливаем wasVisited из сохраненного состояния
            visibilityService.restoreVisitedState(currentLevel);
            // Дополнительно обновляем FOV для текущей позиции
            visibilityService.calculateFov(currentLevel, new Position(player.getX(), player.getY()));
        } else {
            // Выключаем туман: сохраняем текущее состояние видимости и wasVisited
            visibilityService.saveVisibilityState(currentLevel);
            visibilityService.saveVisitedState(currentLevel);
            // Показываем всю карту
            visibilityService.revealAll(currentLevel);
        }
    }

    private void showDeathMenu(Screen screen) throws Exception {
        DeathMenuScreen deathMenu = new DeathMenuScreen(statisticsRepository);
        deathMenu.show(screen, gameSession);
    }

    private void showVictoryMenu(Screen screen) throws Exception {
        ScoresMenuScreen victoryMenu = new ScoresMenuScreen(statisticsRepository);
        victoryMenu.show(screen);
    }

    private void handleMove(Direction direction) {
        if (selectionState != SelectionState.NONE) return;

        MoveResult result =
                movementService.resolvePlayerMovement(
                        gameSession.getPlayer(),
                        direction,
                        gameSession.getCurrentLevel(),
                        gameSession.getSessionId());

        if (result == null) {
            return;
        }

        playerActed =
                result.getType() != MoveResultType.BLOCKED && result.getType() != MoveResultType.WAIT;

        switch (result.getType()) {
            case NEXT_LEVEL:
                fogOfWarEnabled = true;
                gameSessionService.goToNextLevel(gameSession);
                Player player = gameSession.getPlayer();
                Level newLevel = gameSession.getCurrentLevel();
                visibilityService.initializeLevel(newLevel, new Position(player.getX(), player.getY()));
                notifications.addFirst(
                        "Переход на следующий уровень! Сложность: "
                                + difficultyBalanceService.getDifficultyDescription());
                break;
            case ATTACK_KILL:
                notifications.addFirst(result.getMessage() + " Монстр побеждён!");
                break;
            case ATTACK_HIT:
                notifications.addFirst(result.getMessage());
                break;
            case DOOR_OPENED:
                notifications.addFirst(result.getMessage());
                playerActed = true;
                break;
            case DOOR_LOCKED:
                notifications.addFirst(result.getMessage());
                playerActed = false;
                break;
            case KEY_FOUND:
                notifications.addFirst(result.getMessage());
                break;
            case PICKUP:
                Item item = result.getItem();
                if (item != null) {
                    notifications.addFirst("Подобран предмет: " + item.getName());
                    statisticsService.updateItemPickup(gameSession.getSessionId(), item.getType());
                } else {
                    notifications.addFirst("Подобран предмет, но ошибка отображения");
                }
                break;
            case MOVE:
                statisticsService.updateStepCount(gameSession.getSessionId());
                break;
            case BLOCKED:
                notifications.addFirst("Туда нельзя пройти!");
                break;
            case WAIT:
                break;
        }

        while (notifications.size() > MAX_NOTIFICATIONS) {
            notifications.removeLast();
        }
        gameSessionService.checkGameState(gameSession);
    }

    private void handleInput(KeyStroke key) {
        if (key == null) return;

        if (!gameSession.isActive()) return;

        switch (key.getKeyType()) {
            case ArrowUp -> handleMove(Direction.UP);
            case ArrowDown -> handleMove(Direction.DOWN);
            case ArrowLeft -> handleMove(Direction.LEFT);
            case ArrowRight -> handleMove(Direction.RIGHT);
            case Character -> {
                char c = java.lang.Character.toLowerCase(key.getCharacter());
                Direction moveDir =
                        switch (c) {
                            case 'w' -> Direction.UP;
                            case 's' -> Direction.DOWN;
                            case 'a' -> Direction.LEFT;
                            case 'd' -> Direction.RIGHT;
                            default -> null;
                        };

                if (moveDir != null) {
                    handleMove(moveDir);
                } else {
                    handleCharacterInput(c);
                }
            }
            case Escape -> gameSession.gameOver();
        }
    }

    private void teleportToLastLevel() {
        LevelGenerator levelGenerator = gameSessionService.getLevelGenerator();

        boolean success =
                levelService.teleportToLastLevel(
                        gameSession,
                        levelGenerator,
                        difficultyBalanceService,
                        visibilityService,
                        statisticsService);

        if (success) {
            gameSession.setGameState(GameState.PLAYING);

            notifications.addFirst("ТЕЛЕПОРТАЦИЯ НА 21 УРОВЕНЬ!");
            notifications.addFirst("Будьте осторожны, это финальное испытание!");
            playerActed = true;
        } else {
            notifications.addFirst("Ошибка телепортации!");
        }
    }

    private void handleCharacterInput(char c) {

        if (c == 'l') {
            teleportToLastLevel();
        }

        SelectionState newMode = null;
        switch (c) {
            case 'k' -> newMode = SELECT_ELIXIR;
            case 'e' -> newMode = SELECT_SCROLL;
            case 'h' -> newMode = SELECT_WEAPON;
            case 'j' -> newMode = SELECT_FOOD;
            case 'u' -> newMode = SELECT_KEY;
            case 'i' -> newMode = NONE;
        }

        if (c == 'f') {
            toggleFogOfWar();

            Level currentLevel = gameSession.getCurrentLevel();
            Player player = gameSession.getPlayer();

            if (fogOfWarEnabled) {
                // Туман включен - рассчитываем видимость
                visibilityService.calculateFov(currentLevel, new Position(player.getX(), player.getY()));
            } else {
                // Туман выключен - показываем всю карту
                visibilityService.revealAll(currentLevel);
            }

            notifications.addFirst(fogOfWarEnabled ? "Туман войны включен" : "Туман войны выключен");
            return;
        }

        // закрытие инвентаря или просто переключение страницы инвентаря
        if (newMode != null) {
            if (selectionState == newMode) {
                selectionState = SelectionState.NONE;
            } else {
                selectionState = newMode;
            }
            return;
        }

        if (selectionState != SelectionState.NONE) {
            if (c >= '1' && c <= '9') {
                int slot = c - '1';
                useInventoryItem(slot);
                selectionState = SelectionState.NONE;
            } else if (selectionState == SELECT_WEAPON && c == '0') {
                unequipWeapon();
                selectionState = SelectionState.NONE;
            } else if (selectionState == SELECT_KEY && c == '0') {
                useKeyOnAdjacentDoor();
                selectionState = SelectionState.NONE;
            }
        }
    }

    private void useInventoryItem(int slot) {
        Player player = gameSession.getPlayer();
        Level level = gameSession.getCurrentLevel();
        ItemUseResult result;

        switch (selectionState) {
            case SELECT_FOOD:
                result = itemService.useFood(player, slot, level, gameSession.getSessionId());
                break;
            case SELECT_ELIXIR:
                result =
                        itemService.useElixir(
                                player, slot, player.getTurnsLived(), level, gameSession.getSessionId());
                break;
            case SELECT_SCROLL:
                result = itemService.useScroll(player, slot, level, gameSession.getSessionId());
                break;
            case SELECT_WEAPON:
                EquipResult equipResult =
                        itemService.equipWeapon(player, slot, level, gameSession.getSessionId());
                if (equipResult.isSuccess()) {
                    notifications.addFirst(equipResult.getMessage());
                    playerActed = true;
                } else {
                    notifications.addFirst(equipResult.getMessage());
                    playerActed = true;
                }
                return;
            case SELECT_KEY:
                // Используем ключ
                useKeyFromSlot(slot);
                return;
            default:
                return;
        }

        if (result != null) {
            if (result.isSuccess()) {
                notifications.addFirst(result.getMessage());
                playerActed = true;
            } else {
                notifications.addFirst(result.getMessage());
                playerActed = false;
            }
        }
    }

    private void useKeyFromSlot(int slot) {
        Player player = gameSession.getPlayer();
        Backpack backpack = player.getBackpack();

        if (backpack == null) {
            notifications.addFirst("Нет рюкзака!");
            return;
        }

        List<Item> keys = getAllKeysFromBackpack(backpack);

        if (slot >= keys.size()) {
            notifications.addFirst("Нет ключа в этом слоте!");
            return;
        }

        Item keyItem = keys.get(slot);
        if (!(keyItem instanceof Key)) {
            notifications.addFirst("Это не ключ!");
            return;
        }

        Key key = (Key) keyItem;
        KeyColor keyColor = key.getColor();

        Position playerPos = new Position(player.getX(), player.getY());
        Door targetDoor = findAdjacentDoor(gameSession.getCurrentLevel(), playerPos);

        if (targetDoor == null) {
            notifications.addFirst("Рядом нет двери!");
            return;
        }

        if (targetDoor.isOpen()) {
            notifications.addFirst("Эта дверь уже открыта!");
            return;
        }

        if (targetDoor.getColor() != keyColor) {
            notifications.addFirst(
                    String.format(
                            "Этот ключ не подходит! Нужен %s ключ.", targetDoor.getColor().getDisplayName()));
            return;
        }

        doorService.openDoor(targetDoor);
        removeKeyFromBackpack(backpack, key);

        notifications.addFirst(String.format("Дверь открыта %s ключом!", keyColor.getDisplayName()));
        playerActed = true;
    }

    private void useKeyOnAdjacentDoor() {
        Player player = gameSession.getPlayer();
        Backpack backpack = player.getBackpack();

        if (backpack == null) {
            notifications.addFirst("Нет рюкзака!");
            return;
        }

        Position playerPos = new Position(player.getX(), player.getY());
        Door targetDoor = findAdjacentDoor(gameSession.getCurrentLevel(), playerPos);

        if (targetDoor == null) {
            notifications.addFirst("Рядом нет двери!");
            return;
        }

        if (targetDoor.isOpen()) {
            notifications.addFirst("Эта дверь уже открыта!");
            return;
        }

        KeyColor doorColor = targetDoor.getColor();

        if (doorService.canOpenDoor(player, doorColor)) {
            doorService.openDoor(targetDoor);
            doorService.useKey(player, doorColor);

            notifications.addFirst(String.format("Дверь открыта %s ключом!", doorColor.getDisplayName()));
            playerActed = true;
        } else {
            notifications.addFirst(
                    String.format("Нет %s ключа для открытия двери!", doorColor.getDisplayName()));
            playerActed = false;
        }
    }

    private Door findAdjacentDoor(Level level, Position playerPos) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int x = playerPos.x() + dir[0];
            int y = playerPos.y() + dir[1];

            Door door = level.getDoorAt(x, y);
            if (door != null) {
                return door;
            }
        }

        return null;
    }

    private List<Item> getAllKeysFromBackpack(Backpack backpack) {
        List<Item> keys = new ArrayList<>();

        for (Item item : backpack.getFood()) {
            if (item instanceof Key) keys.add(item);
        }
        for (Item item : backpack.getElixirs()) {
            if (item instanceof Key) keys.add(item);
        }
        for (Item item : backpack.getScrolls()) {
            if (item instanceof Key) keys.add(item);
        }
        for (Item item : backpack.getWeapons()) {
            if (item instanceof Key) keys.add(item);
        }

        return keys;
    }

    private void removeKeyFromBackpack(Backpack backpack, Key keyToRemove) {
        List<Item> allItems = new ArrayList<>();
        allItems.addAll(backpack.getFood());
        allItems.addAll(backpack.getElixirs());
        allItems.addAll(backpack.getScrolls());
        allItems.addAll(backpack.getWeapons());

        for (int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);
            if (item == keyToRemove) {
                if (i < backpack.getFood().size()) {
                    backpack.removeFood(i);
                } else if (i < backpack.getFood().size() + backpack.getElixirs().size()) {
                    int elixirIndex = i - backpack.getFood().size();
                    backpack.removeElixir(elixirIndex);
                } else if (i
                        < backpack.getFood().size()
                        + backpack.getElixirs().size()
                        + backpack.getScrolls().size()) {
                    int scrollIndex = i - backpack.getFood().size() - backpack.getElixirs().size();
                    backpack.removeScroll(scrollIndex);
                } else {
                    int weaponIndex =
                            i
                                    - backpack.getFood().size()
                                    - backpack.getElixirs().size()
                                    - backpack.getScrolls().size();
                    backpack.removeWeapon(weaponIndex);
                }
                break;
            }
        }
    }

    private void unequipWeapon() {
        EquipResult result =
                itemService.equipWeapon(
                        gameSession.getPlayer(), -1, gameSession.getCurrentLevel(), gameSession.getSessionId());
        if (result.isSuccess()) {
            notifications.addFirst(result.getMessage());
        } else {
            notifications.addFirst(result.getMessage());
        }
    }
}
