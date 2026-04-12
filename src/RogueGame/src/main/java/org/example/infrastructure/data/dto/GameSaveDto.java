package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO для сохранения полной игровой сессии в JSON. Содержит всю информацию, необходимую для
 * восстановления игры.
 */
public class GameSaveDto {
    private long sessionId;
    private int currentLevelNumber;
    private String gameState; // PLAYING, GAME_OVER, VICTORY, SLEEPING и т.д.

    private PlayerDto player;
    private LevelDto level;
    private StatisticsDto statistics;

    // Для обратной совместимости и дополнительных данных
    private List<MonsterDto> monsters;
    private List<ItemDto> floorItems;
    private List<TemporaryEffectDto> activeEffects;

    public GameSaveDto() {
        this.monsters = new ArrayList<>();
        this.floorItems = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public void setCurrentLevelNumber(int level) {
        this.currentLevelNumber = level;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public PlayerDto getPlayer() {
        return player;
    }

    public void setPlayer(PlayerDto player) {
        this.player = player;
    }

    public LevelDto getLevel() {
        return level;
    }

    public void setLevel(LevelDto level) {
        this.level = level;
    }

    public StatisticsDto getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticsDto statistics) {
        this.statistics = statistics;
    }

    public List<MonsterDto> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<MonsterDto> monsters) {
        this.monsters = monsters;
    }

    public List<ItemDto> getFloorItems() {
        return floorItems;
    }

    public void setFloorItems(List<ItemDto> floorItems) {
        this.floorItems = floorItems;
    }

    public List<TemporaryEffectDto> getActiveEffects() {
        return activeEffects;
    }

    public void setActiveEffects(List<TemporaryEffectDto> activeEffects) {
        this.activeEffects = activeEffects;
    }
}
