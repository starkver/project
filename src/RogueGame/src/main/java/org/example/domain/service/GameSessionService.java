package org.example.domain.service;

import java.util.List;
import java.util.Random;
import org.example.domain.generator.LevelGenerator;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.GameState;
import org.example.infrastructure.data.repository.GameSaveRepository;
import org.example.infrastructure.data.repository.StatisticsRepository;

/**
 * Сервис отвечающий за управление игровой сессией. Обрабатывает создание новой игры, загрузку,
 * сохранение, переход между уровнями и проверку состояния игры.
 */
public class GameSessionService {

    private final LevelGenerator levelGenerator;
    private final StatisticsService statisticsService;
    private final DifficultyBalanceService difficultyBalanceService;

    private final GameSaveRepository saveRepository;
    private final StatisticsRepository statsRepository;

    public LevelGenerator getLevelGenerator() {
        return levelGenerator;
    }

    /** Конструктор с возможностью внедрения DifficultyBalanceService (для тестирования). */
    public GameSessionService(
            LevelGenerator levelGenerator,
            StatisticsService statisticsService,
            StatisticsRepository statsRepository,
            DifficultyBalanceService difficultyBalanceService) {
        this.levelGenerator = levelGenerator;
        this.statisticsService = statisticsService;
        this.statsRepository = statsRepository;
        this.saveRepository = new GameSaveRepository();
        this.difficultyBalanceService = difficultyBalanceService;
    }

    /** Создает новую игровую сессию. */
    public GameSession createNewGame() {
        // Сбрасываем балансировку для новой игры
        difficultyBalanceService.reset();

        // Создаем игрока с базовыми характеристиками
        Player player = createNewPlayer();

        // Генерируем уникальный ID сессии
        long sessionId = System.currentTimeMillis();

        // Создаем статистику для сессии
        GameStatistics statistics = statisticsService.createStatistics(sessionId);

        // Получаем коэффициент сложности для первого уровня
        double difficultyMultiplier = difficultyBalanceService.getDifficultyMultiplier();
        double itemCountMultiplier = difficultyBalanceService.getItemCountMultiplier();
        double monsterCountMultiplier = difficultyBalanceService.getMonsterCountMultiplier();
        double usefulItemChanceMultiplier = difficultyBalanceService.getUsefulItemChanceMultiplier();
        double monsterDifficultyMultiplier = difficultyBalanceService.getMonsterDifficultyMultiplier();
        double elixirChanceMultiplier = difficultyBalanceService.getElixirChanceMultiplier();

        // Генерируем первый уровень с учётом балансировки
        Level currentLevel =
                levelGenerator.levelGenerate(
                        1,
                        difficultyMultiplier,
                        itemCountMultiplier,
                        monsterCountMultiplier,
                        usefulItemChanceMultiplier,
                        monsterDifficultyMultiplier,
                        elixirChanceMultiplier);

        // Размещаем игрока в стартовой комнате
        Position startPosition = findStartPosition(currentLevel);
        player.moveTo(startPosition.x(), startPosition.y());

        // Обновляем позицию игрока на карте
        currentLevel.getTileMap().setPlayerPosition(startPosition.x(), startPosition.y());

        // Отмечаем стартовую клетку как посещённую
        Tile startTile = currentLevel.getTileMap().getTile(startPosition);
        if (startTile != null) {
            startTile.setWasVisited(true);
        }

        // Создаем сессию
        GameSession session =
                new GameSession(
                        sessionId,
                        player,
                        currentLevel,
                        1, // текущий уровень
                        GameState.PLAYING,
                        statistics);

        return session;
    }

    /** Создает нового игрока с базовыми характеристиками. */
    private Player createNewPlayer() {
        Player player = new Player(0, 0); // Координаты будут установлены позже

        // Базовые характеристики из задания
        player.setHealth(100);
        player.setMaxHealth(100);
        player.setAgility(10);
        player.setStrength(10);

        // Создаем пустой рюкзак
        player.setBackpack(new Backpack());

        return player;
    }

    /**
     * Находит стартовую позицию на уровне. Ищет стартовую комнату и выбирает случайную внутреннюю
     * позицию в ней.
     */
    private Position findStartPosition(Level level) {
        // Получаем позицию стартовой комнаты в сетке
        Position startRoomGridPos = level.getStartRoomPosition();

        if (startRoomGridPos != null) {
            Room startRoom = level.getRoom(startRoomGridPos.x(), startRoomGridPos.y());
            if (startRoom != null) {
                // Получаем случайную позицию внутри комнаты
                List<Position> interiorPositions = startRoom.getAllInteriorPositions();
                if (!interiorPositions.isEmpty()) {
                    Random random = new Random();
                    return interiorPositions.get(random.nextInt(interiorPositions.size()));
                }
                // Если нет внутренних позиций, возвращаем центр комнаты
                return startRoom.getCenter();
            }
        }

        // Если стартовая комната не отмечена или не найдена, ищем любую комнату
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null) {
                    List<Position> interiorPositions = room.getAllInteriorPositions();
                    if (!interiorPositions.isEmpty()) {
                        Random random = new Random();
                        return interiorPositions.get(random.nextInt(interiorPositions.size()));
                    }
                    return room.getCenter();
                }
            }
        }

        // Если комнат нет, возвращаем центр карты (запасной вариант)
        TileMap tileMap = level.getTileMap();
        return new Position(tileMap.getWidth() / 2, tileMap.getHeight() / 2);
    }

    /** Сохранить текущую игру. */
    public void saveGame(GameSession session) {
        if (session == null) return;
        statsRepository.saveSessionStats(session.getStatistics());
        saveRepository.save(session);
    }

    /** Загрузить сохранённую игру. */
    public GameSession loadGame() {
        GameSession session = saveRepository.load();

        if (session != null) {
            GameStatistics loadedStats = statsRepository.loadSessionStats();
            GameStatistics currentStats = session.getStatistics();

            if (loadedStats != null && currentStats != null) {
                copyStatistics(loadedStats, currentStats);
            }
        }

        return session;
    }

    /** Проверить, есть ли сохранение. */
    public boolean hasSave() {
        return saveRepository.exists();
    }

    /** Удалить сохранение. */
    public void deleteSave() {
        saveRepository.delete();
    }

    /** Копирует данные из одной статистики в другую. */
    private void copyStatistics(GameStatistics source, GameStatistics target) {
        target.setLevelReached(source.getLevelReached());
        target.setTotalTreasureCollected(source.getTotalTreasureCollected());
        target.setEnemiesDefeated(source.getEnemiesDefeated());
        target.setFoodEaten(source.getFoodEaten());
        target.setElixirsDrunk(source.getElixirsDrunk());
        target.setScrollsRead(source.getScrollsRead());
        target.setStepsTaken(source.getStepsTaken());
        target.setExplorationPercent(source.getExplorationPercent());
        target.setDamageDealt(source.getDamageDealt());
        target.setDamageTaken(source.getDamageTaken());
        target.setHitsDealt(source.getHitsDealt());
        target.setHitsTaken(source.getHitsTaken());
        target.setMaxHealthReached(source.getMaxHealthReached());
        target.setTotalHealing(source.getTotalHealing());

        // Копируем мапы статистики
        source.getKillsByMonsterType().forEach(target::addKill);
        source.getFoodEatenByType().forEach(target::addFoodEaten);
        source.getElixirsByType().forEach(target::addElixirDrunk);
        source.getScrollsByType().forEach(target::addScrollRead);
        source.getWeaponsUsed().forEach(target::addWeaponUsed);
        source.getItemsPickedUp().forEach(target::addItemPickup);
    }

    /**
     * Обрабатывает переход на следующий уровень.
     *
     * @param session игровая сессия
     */
    public void goToNextLevel(GameSession session) {
        int currentLevelNumber = session.getCurrentLevelNumber();

        // Проверяем, не последний ли это уровень
        if (isPlayerWin(session, currentLevelNumber)) return;

        // Устанавливаем состояние перехода
        session.setGameState(GameState.LEVEL_TRANSITION);

        // Обновляем статистику исследования перед уходом с уровня
        updateExplorationStat(session);

        // Получаем метрики для следующего уровня
        int nextLevelNumber = currentLevelNumber + 1;

        // Регистрируем начало нового уровня и получаем коэффициенты сложности
        double difficultyMultiplier = difficultyBalanceService.registerLevelStart(nextLevelNumber);
        double itemCountMultiplier = difficultyBalanceService.getItemCountMultiplier();
        double monsterCountMultiplier = difficultyBalanceService.getMonsterCountMultiplier();
        double usefulItemChanceMultiplier = difficultyBalanceService.getUsefulItemChanceMultiplier();
        double monsterDifficultyMultiplier = difficultyBalanceService.getMonsterDifficultyMultiplier();
        double elixirChanceMultiplier = difficultyBalanceService.getElixirChanceMultiplier();

        System.out.println("=== Адаптация сложности ===");
        System.out.println(
                "Уровень "
                        + nextLevelNumber
                        + " сложность: "
                        + difficultyBalanceService.getDifficultyDescription());
        System.out.println("Множитель: " + difficultyMultiplier);
        System.out.println("Множитель для числа предметов: " + itemCountMultiplier);
        System.out.println("Множитель для числа монстров: " + monsterCountMultiplier);

        // Генерируем следующий уровень с учётом балансировки
        Level nextLevel =
                levelGenerator.levelGenerate(
                        nextLevelNumber,
                        difficultyMultiplier,
                        itemCountMultiplier,
                        monsterCountMultiplier,
                        usefulItemChanceMultiplier,
                        monsterDifficultyMultiplier,
                        elixirChanceMultiplier);

        // Находим стартовую позицию на новом уровне
        Position startPosition = findStartPosition(nextLevel);

        // Перемещаем игрока
        Player player = session.getPlayer();
        player.moveTo(startPosition.x(), startPosition.y());

        // Обновляем позицию на карте
        nextLevel.getTileMap().setPlayerPosition(startPosition.x(), startPosition.y());

        // Отмечаем стартовую клетку как посещённую
        Tile startTile = nextLevel.getTileMap().getTile(startPosition);
        if (startTile != null) {
            startTile.setWasVisited(true);
        }

        // Обновляем сессию
        session.setCurrentLevel(nextLevel);
        session.setCurrentLevelNumber(nextLevelNumber);

        // Обновляем статистику
        statisticsService.updateLevelReached(session.getSessionId(), nextLevelNumber);

        saveGame(session);

        // Возвращаемся в состояние игры
        session.setGameState(GameState.PLAYING);
    }

    private boolean isPlayerWin(GameSession session, int currentLevelNumber) {
        if (currentLevelNumber >= 21) {
            // Игрок прошел игру
            session.setGameState(GameState.VICTORY);

            // Обновляем статистику перед завершением
            updateExplorationStat(session);
            return true;
        }
        return false;
    }

    /** Обновляет статистику исследования текущего уровня. */
    private void updateExplorationStat(GameSession session) {
        Level level = session.getCurrentLevel();
        int explorationPercent = calculateExplorationPercentage(level);
        statisticsService.updateExploration(session.getSessionId(), explorationPercent);
    }

    /** Рассчитывает процент исследования уровня. */
    private int calculateExplorationPercentage(Level level) {
        TileMap tileMap = level.getTileMap();
        int totalWalkable = 0;
        int visited = 0;

        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null && tile.isWalkable()) {
                    totalWalkable++;
                    if (tile.wasVisited()) {
                        visited++;
                    }
                }
            }
        }

        if (totalWalkable == 0) return 0;
        return (visited * 100) / totalWalkable;
    }

    /** Проверяет, достиг ли игрок выхода с уровня. */
    public boolean isPlayerOnExit(GameSession session) {
        Player player = session.getPlayer();
        Level currentLevel = session.getCurrentLevel();

        return currentLevel.isExit(player.getX(), player.getY());
    }

    /** Проверяет состояние игры и обновляет его при необходимости. */
    public void checkGameState(GameSession session) {
        Player player = session.getPlayer();
        GameState currentState = session.getGameState();

        // Проверка смерти игрока
        if (!player.isAlive()) {
            session.setGameState(GameState.GAME_OVER);

            statisticsService.syncTreasuresFromBackpack(session.getSessionId(), player.getBackpack());
            statisticsService.syncLevelReached(session.getSessionId(), session.getCurrentLevelNumber());
            statisticsService.updateStepCount(session.getSessionId());

            // Обновляем статистику перед завершением
            updateExplorationStat(session);
            statisticsService.finishStatistics(session.getSessionId());
            deleteSave();

            return;
        }

        // Проверка перехода на следующий уровень
        if (currentState == GameState.PLAYING && isPlayerOnExit(session)) {
            goToNextLevel(session);
        }
    }

    /** Получает сервис балансировки сложности. */
    public DifficultyBalanceService getDifficultyBalanceService() {
        return difficultyBalanceService;
    }
}
