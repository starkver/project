package org.example.domain.service;

import org.example.domain.model.enums.GameState;

/** Краткая информация о сессии для отображения в меню загрузки. */
class GameSessionInfo {
    private final long sessionId;
    private final int level;
    private final int health;
    private final int maxHealth;
    private final int strength;
    private final int agility;
    private final GameState state;

    public GameSessionInfo(
            long sessionId,
            int level,
            int health,
            int maxHealth,
            int strength,
            int agility,
            GameState state) {
        this.sessionId = sessionId;
        this.level = level;
        this.health = health;
        this.maxHealth = maxHealth;
        this.strength = strength;
        this.agility = agility;
        this.state = state;
    }

    public long getSessionId() {
        return sessionId;
    }

    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public GameState getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format(
                "Уровень %d | HP: %d/%d | Сила: %d | Ловкость: %d",
                level, health, maxHealth, strength, agility);
    }
}
