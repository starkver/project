package org.example.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.domain.model.entity.Backpack;
import org.example.domain.model.entity.GameStatistics;
import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;
import org.example.domain.model.enums.MonsterType;
import org.example.infrastructure.data.repository.StatisticsRepository;

/**
 * Сервис отвечающий за сбор и обработку игровой статистики. Отслеживает все действия игрока и
 * формирует таблицу рекордов.
 */
public class StatisticsService {

    private final Map<Long, GameStatistics> activeSessions = new HashMap<>();
    private final List<GameStatistics> allTimeStatistics = new ArrayList<>();

    private final StatisticsRepository repository;

    public StatisticsService() {
        this.repository = new StatisticsRepository();
    }

    /** Создает новую статистику для игровой сессии. */
    public GameStatistics createStatistics(long sessionId) {
        GameStatistics stats = new GameStatistics(sessionId);
        activeSessions.put(sessionId, stats);
        return stats;
    }

    /** Получает статистику для сессии. */
    public GameStatistics getStatistics(long sessionId) {
        return activeSessions.get(sessionId);
    }

    /** Завершает сессию статистики и добавляет её в общий список. */
    public void finishStatistics(long sessionId) {
        GameStatistics stats = activeSessions.remove(sessionId);
        if (stats != null) {
            stats.setEndTime(LocalDateTime.now());
            allTimeStatistics.add(stats);

            // Сохранить в statistics.json
            repository.saveSessionStats(stats);

            // Обновить таблицу рекордов в scoreboard.json
            repository.updateScoreboard(stats);
        }
    }

    /**
     * Обновляет статистику после перемещения игрока. Вызывается из GameController или
     * MovementService.
     */
    public void updateStepCount(long sessionId) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setStepsTaken(stats.getStepsTaken() + 1);
        }
    }

    public void updateEnemyDefeated(long sessionId, MonsterType monsterType, int treasureValue) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setEnemiesDefeated(stats.getEnemiesDefeated() + 1);
            stats.setTotalTreasureCollected(stats.getTotalTreasureCollected() + treasureValue);
            stats.addKill(monsterType, 1);
        }
    }

    public void updateFoodEaten(long sessionId, ItemSubType foodType, int healAmount) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setFoodEaten(stats.getFoodEaten() + 1);
            stats.setTotalHealing(stats.getTotalHealing() + healAmount);
            stats.addFoodEaten(foodType, 1);
        }
    }

    /** Синхронизирует количество сокровищ из рюкзака игрока со статистикой. */
    public void syncTreasuresFromBackpack(long sessionId, Backpack backpack) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null && backpack != null) {
            int totalTreasure = backpack.getTotalTreasureValue();
            stats.setTotalTreasureCollected(totalTreasure);
        }
    }

    /** Синхронизирует достигнутый уровень. */
    public void syncLevelReached(long sessionId, int levelNumber) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setLevelReached(levelNumber);
        }
    }

    /** Обновляет статистику после использования эликсира. Вызывается из ItemService. */
    public void updateElixirDrunk(long sessionId, ItemSubType elixirType) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setElixirsDrunk(stats.getElixirsDrunk() + 1);
            stats.addElixirDrunk(elixirType, 1);
        }
    }

    /** Обновляет статистику после использования свитка. Вызывается из ItemService. */
    public void updateScrollRead(long sessionId, ItemSubType scrollType) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setScrollsRead(stats.getScrollsRead() + 1);
            stats.addScrollRead(scrollType, 1);
        }
    }

    /** Обновляет статистику после экипировки оружия. Вызывается из ItemService. */
    public void updateWeaponEquipped(long sessionId, ItemSubType weaponType) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.addWeaponUsed(weaponType, 1);
        }
    }

    /** Обновляет статистику после подбора предмета. Вызывается из ItemService. */
    public void updateItemPickup(long sessionId, ItemType itemType) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.addItemPickup(itemType, 1);
        }
    }

    /** Обновляет статистику после перехода на новый уровень. Вызывается из GameSessionService. */
    public void updateLevelReached(long sessionId, int levelNumber) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setLevelReached(levelNumber);
        }
    }

    /**
     * Обновляет статистику исследования уровня. Вызывается из GameSessionService или
     * VisibilityService.
     */
    public void updateExploration(long sessionId, int explorationPercent) {
        GameStatistics stats = getStatistics(sessionId);
        if (stats != null) {
            stats.setExplorationPercent(explorationPercent);
        }
    }
}
