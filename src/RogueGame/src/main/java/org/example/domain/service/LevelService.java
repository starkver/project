package org.example.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.domain.generator.LevelGenerator;
import org.example.domain.model.entity.*;

/**
 * Сервис отвечающий за управление текущим уровнем. Обрабатывает добавление/удаление объектов на
 * уровне, поиск сущностей и проверку состояния уровня.
 */
public class LevelService {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 21;

    private final Random random = new Random();

    /** Находит всех живых монстров на уровне. */
    public List<Monster> findAliveMonsters(Level level) {
        return level.getAliveMonsters();
    }

    /** Находит комнату по позиции. */
    public Room findRoomAt(Level level, Position position) {
        return level.findRoomContaining(position.x(), position.y());
    }

    /** Генерирует случайную позицию в указанной комнате. */
    public Position getRandomPositionInRoom(Room room) {
        return room.getRandomInteriorPosition(random);
    }

    /** Генерирует случайную позицию в комнате, не занятую монстром. */
    public Position getRandomFreePositionInRoom(Level level, Room room) {
        List<Position> interiorPositions = room.getAllInteriorPositions();
        List<Position> freePositions = new ArrayList<>();

        for (Position pos : interiorPositions) {
            if (!level.getTileMap().hasMonster(pos.x(), pos.y())) {
                freePositions.add(pos);
            }
        }

        if (freePositions.isEmpty()) {
            return getRandomPositionInRoom(room);
        }

        return freePositions.get(random.nextInt(freePositions.size()));
    }

    /** Телепортирует игрока на указанный уровень. Используется для отладки и тестирования. */
    public boolean teleportToLevel(
            GameSession gameSession,
            int targetLevelNumber,
            LevelGenerator levelGenerator,
            DifficultyBalanceService difficultyBalanceService,
            VisibilityService visibilityService,
            StatisticsService statisticsService) {

        if (targetLevelNumber < MIN_LEVEL || targetLevelNumber > MAX_LEVEL) {
            return false;
        }

        // Получаем коэффициенты сложности для целевого уровня
        double difficultyMultiplier = difficultyBalanceService.getDifficultyMultiplier();
        double itemCountMultiplier = difficultyBalanceService.getItemCountMultiplier();
        double monsterCountMultiplier = difficultyBalanceService.getMonsterCountMultiplier();
        double usefulItemChanceMultiplier = difficultyBalanceService.getUsefulItemChanceMultiplier();
        double monsterDifficultyMultiplier = difficultyBalanceService.getMonsterDifficultyMultiplier();
        double elixirChanceMultiplier = difficultyBalanceService.getElixirChanceMultiplier();

        // Генерируем целевой уровень
        Level targetLevel =
                levelGenerator.levelGenerate(
                        targetLevelNumber,
                        difficultyMultiplier,
                        itemCountMultiplier,
                        monsterCountMultiplier,
                        usefulItemChanceMultiplier,
                        monsterDifficultyMultiplier,
                        elixirChanceMultiplier);

        Position startPosition = levelGenerator.findSafeStartPosition(targetLevel);

        // Перемещаем игрока
        Player player = gameSession.getPlayer();
        player.moveTo(startPosition.x(), startPosition.y());

        // Обновляем позицию на карте
        targetLevel.getTileMap().setPlayerPosition(startPosition.x(), startPosition.y());

        // Отмечаем стартовую клетку как посещённую
        Tile startTile = targetLevel.getTileMap().getTile(startPosition);
        if (startTile != null) {
            startTile.setWasVisited(true);
        }

        // Обновляем сессию
        gameSession.setCurrentLevel(targetLevel);
        gameSession.setCurrentLevelNumber(targetLevelNumber);

        // Обновляем статистику
        statisticsService.updateLevelReached(gameSession.getSessionId(), targetLevelNumber);

        // Инициализируем видимость для нового уровня
        visibilityService.initializeLevel(targetLevel, startPosition);

        return true;
    }

    /** Телепортирует игрока на 21 уровень (финальный уровень). */
    public boolean teleportToLastLevel(
            GameSession gameSession,
            LevelGenerator levelGenerator,
            DifficultyBalanceService difficultyBalanceService,
            VisibilityService visibilityService,
            StatisticsService statisticsService) {
        return teleportToLevel(
                gameSession,
                MAX_LEVEL,
                levelGenerator,
                difficultyBalanceService,
                visibilityService,
                statisticsService);
    }
}
