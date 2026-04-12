package org.example.domain.model.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;
import org.example.domain.model.enums.MonsterType;

/**
 * Класс, представляющий статистику игровой сессии. Содержит все метрики, собираемые в процессе
 * игры.
 */
public class GameStatistics {

    private final long sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int levelReached; // достигнутый уровень (1-21)
    private int totalTreasureCollected; // общее количество собранных сокровищ
    private int enemiesDefeated; // количество убитых врагов
    private int stepsTaken; // количество пройденных клеток
    private int explorationPercent; // процент исследования уровня (0-100)

    private int damageDealt; // нанесённый урон
    private int damageTaken; // полученный урон
    private int hitsDealt; // количество попаданий по врагам
    private int hitsTaken; // количество попаданий от врагов
    private int maxHealthReached; // максимальное достигнутое HP
    private int totalHealing; // общее количество вылеченного HP

    private int foodEaten; // количество съеденной еды
    private int elixirsDrunk; // количество выпитых эликсиров
    private int scrollsRead; // количество прочитанных свитков

    private final Map<MonsterType, Integer> killsByMonsterType;
    private final Map<ItemSubType, Integer> foodEatenByType;
    private final Map<ItemSubType, Integer> elixirsDrunkByType;
    private final Map<ItemSubType, Integer> scrollsReadByType;
    private final Map<ItemSubType, Integer> weaponsUsed;
    private final Map<ItemType, Integer> itemsPickedUp;

    public GameStatistics(long sessionId) {
        this.sessionId = sessionId;
        this.startTime = LocalDateTime.now();
        this.endTime = null;

        // Инициализация основных полей
        this.levelReached = 1;
        this.totalTreasureCollected = 0;
        this.enemiesDefeated = 0;
        this.stepsTaken = 0;
        this.explorationPercent = 0;

        this.damageDealt = 0;
        this.damageTaken = 0;
        this.hitsDealt = 0;
        this.hitsTaken = 0;
        this.maxHealthReached = 0;
        this.totalHealing = 0;

        this.foodEaten = 0;
        this.elixirsDrunk = 0;
        this.scrollsRead = 0;

        // Инициализация мап
        this.killsByMonsterType = new HashMap<>();
        this.foodEatenByType = new HashMap<>();
        this.elixirsDrunkByType = new HashMap<>();
        this.scrollsReadByType = new HashMap<>();
        this.weaponsUsed = new HashMap<>();
        this.itemsPickedUp = new HashMap<>();
    }

    public long getSessionId() {
        return sessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public int getTotalTreasureCollected() {
        return totalTreasureCollected;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public int getExplorationPercent() {
        return explorationPercent;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public int getHitsDealt() {
        return hitsDealt;
    }

    public int getHitsTaken() {
        return hitsTaken;
    }

    public int getMaxHealthReached() {
        return maxHealthReached;
    }

    public int getTotalHealing() {
        return totalHealing;
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public int getElixirsDrunk() {
        return elixirsDrunk;
    }

    public int getScrollsRead() {
        return scrollsRead;
    }

    public Map<MonsterType, Integer> getKillsByMonsterType() {
        return new HashMap<>(killsByMonsterType);
    }

    public Map<ItemSubType, Integer> getFoodEatenByType() {
        return new HashMap<>(foodEatenByType);
    }

    public Map<ItemSubType, Integer> getElixirsByType() {
        return new HashMap<>(elixirsDrunkByType);
    }

    public Map<ItemSubType, Integer> getScrollsByType() {
        return new HashMap<>(scrollsReadByType);
    }

    public Map<ItemSubType, Integer> getWeaponsUsed() {
        return new HashMap<>(weaponsUsed);
    }

    public Map<ItemType, Integer> getItemsPickedUp() {
        return new HashMap<>(itemsPickedUp);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setLevelReached(int levelReached) {
        if (levelReached >= 1 && levelReached <= 21) {
            this.levelReached = levelReached;
        }
    }

    public void setTotalTreasureCollected(int totalTreasureCollected) {
        this.totalTreasureCollected = totalTreasureCollected;
    }

    public void setEnemiesDefeated(int enemiesDefeated) {
        this.enemiesDefeated = enemiesDefeated;
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;
    }

    public void setExplorationPercent(int explorationPercent) {
        this.explorationPercent = Math.min(100, Math.max(0, explorationPercent));
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }

    public void setHitsDealt(int hitsDealt) {
        this.hitsDealt = hitsDealt;
    }

    public void setHitsTaken(int hitsTaken) {
        this.hitsTaken = hitsTaken;
    }

    public void setMaxHealthReached(int maxHealthReached) {
        if (maxHealthReached > this.maxHealthReached) {
            this.maxHealthReached = maxHealthReached;
        }
    }

    public void setTotalHealing(int totalHealing) {
        this.totalHealing = totalHealing;
    }

    public void setFoodEaten(int foodEaten) {
        this.foodEaten = foodEaten;
    }

    public void setElixirsDrunk(int elixirsDrunk) {
        this.elixirsDrunk = elixirsDrunk;
    }

    public void setScrollsRead(int scrollsRead) {
        this.scrollsRead = scrollsRead;
    }

    public void addKill(MonsterType type, int count) {
        this.enemiesDefeated += count;
        this.killsByMonsterType.merge(type, count, Integer::sum);
    }

    public void addFoodEaten(ItemSubType type, int count) {
        this.foodEaten += count;
        this.foodEatenByType.merge(type, count, Integer::sum);
    }

    public void addElixirDrunk(ItemSubType type, int count) {
        this.elixirsDrunk += count;
        this.elixirsDrunkByType.merge(type, count, Integer::sum);
    }

    public void addScrollRead(ItemSubType type, int count) {
        this.scrollsRead += count;
        this.scrollsReadByType.merge(type, count, Integer::sum);
    }

    public void addWeaponUsed(ItemSubType type, int count) {
        this.weaponsUsed.merge(type, count, Integer::sum);
    }

    public void addItemPickup(ItemType type, int count) {
        this.itemsPickedUp.merge(type, count, Integer::sum);
    }

    @Override
    public String toString() {
        return String.format(
                "GameStatistics{id=%d, level=%d, treasure=%d, enemies=%d, steps=%d}",
                sessionId, levelReached, totalTreasureCollected, enemiesDefeated, stepsTaken);
    }
}
