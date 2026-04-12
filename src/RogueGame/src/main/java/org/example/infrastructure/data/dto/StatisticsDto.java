package org.example.infrastructure.data.dto;

import java.util.HashMap;
import java.util.Map;

/** DTO для статистики сессии. Содержит полную информацию о прохождении. */
public class StatisticsDto {
    private long sessionId;
    private String startTime; // ISO формат: "2024-01-15T10:30:00"
    private String endTime; // ISO формат, может быть null

    // Основные показатели
    private int levelReached;
    private int totalTreasureCollected;
    private int enemiesDefeated;
    private int stepsTaken;
    private int explorationPercent;

    // Боевая статистика
    private int damageDealt;
    private int damageTaken;
    private int hitsDealt;
    private int hitsTaken;

    // Использование предметов
    private int foodEaten;
    private int elixirsDrunk;
    private int scrollsRead;
    private int totalHealing;
    private int maxHealthReached;

    // Детальная статистика (Map со строковыми ключами для безопасной сериализации)
    private Map<String, Integer> killsByMonsterType;
    private Map<String, Integer> foodEatenByType;
    private Map<String, Integer> elixirsByType;
    private Map<String, Integer> scrollsByType;
    private Map<String, Integer> weaponsUsed;
    private Map<String, Integer> itemsPickedUp;

    public StatisticsDto() {
        this.killsByMonsterType = new HashMap<>();
        this.foodEatenByType = new HashMap<>();
        this.elixirsByType = new HashMap<>();
        this.scrollsByType = new HashMap<>();
        this.weaponsUsed = new HashMap<>();
        this.itemsPickedUp = new HashMap<>();
    }

    public StatisticsDto(long sessionId) {
        this();
        this.sessionId = sessionId;
    }

    // Геттеры и сеттеры
    public long getSessionId() {
        return sessionId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getLevelReached() {
        return levelReached;
    }

    public void setLevelReached(int levelReached) {
        this.levelReached = levelReached;
    }

    public int getTotalTreasureCollected() {
        return totalTreasureCollected;
    }

    public void setTotalTreasureCollected(int totalTreasureCollected) {
        this.totalTreasureCollected = totalTreasureCollected;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public void setEnemiesDefeated(int enemiesDefeated) {
        this.enemiesDefeated = enemiesDefeated;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;
    }

    public int getExplorationPercent() {
        return explorationPercent;
    }

    public void setExplorationPercent(int explorationPercent) {
        this.explorationPercent = explorationPercent;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }

    public int getHitsDealt() {
        return hitsDealt;
    }

    public void setHitsDealt(int hitsDealt) {
        this.hitsDealt = hitsDealt;
    }

    public int getHitsTaken() {
        return hitsTaken;
    }

    public void setHitsTaken(int hitsTaken) {
        this.hitsTaken = hitsTaken;
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public void setFoodEaten(int foodEaten) {
        this.foodEaten = foodEaten;
    }

    public int getElixirsDrunk() {
        return elixirsDrunk;
    }

    public void setElixirsDrunk(int elixirsDrunk) {
        this.elixirsDrunk = elixirsDrunk;
    }

    public int getScrollsRead() {
        return scrollsRead;
    }

    public void setScrollsRead(int scrollsRead) {
        this.scrollsRead = scrollsRead;
    }

    public int getTotalHealing() {
        return totalHealing;
    }

    public void setTotalHealing(int totalHealing) {
        this.totalHealing = totalHealing;
    }

    public int getMaxHealthReached() {
        return maxHealthReached;
    }

    public void setMaxHealthReached(int maxHealthReached) {
        this.maxHealthReached = maxHealthReached;
    }

    public Map<String, Integer> getKillsByMonsterType() {
        return killsByMonsterType;
    }

    public void setKillsByMonsterType(Map<String, Integer> killsByMonsterType) {
        this.killsByMonsterType = killsByMonsterType;
    }

    public Map<String, Integer> getFoodEatenByType() {
        return foodEatenByType;
    }

    public void setFoodEatenByType(Map<String, Integer> foodEatenByType) {
        this.foodEatenByType = foodEatenByType;
    }

    public Map<String, Integer> getElixirsByType() {
        return elixirsByType;
    }

    public void setElixirsByType(Map<String, Integer> elixirsByType) {
        this.elixirsByType = elixirsByType;
    }

    public Map<String, Integer> getScrollsByType() {
        return scrollsByType;
    }

    public void setScrollsByType(Map<String, Integer> scrollsByType) {
        this.scrollsByType = scrollsByType;
    }

    public Map<String, Integer> getWeaponsUsed() {
        return weaponsUsed;
    }

    public void setWeaponsUsed(Map<String, Integer> weaponsUsed) {
        this.weaponsUsed = weaponsUsed;
    }

    public Map<String, Integer> getItemsPickedUp() {
        return itemsPickedUp;
    }

    public void setItemsPickedUp(Map<String, Integer> itemsPickedUp) {
        this.itemsPickedUp = itemsPickedUp;
    }
}
