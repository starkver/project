package org.example.domain.service;

import org.example.domain.model.enums.Direction;

/**
 * Состояние ИИ для конкретного монстра. Хранит текущее направление патрулирования, счетчики шагов и
 * т.д.
 */
class MonsterAiState {
    private Direction patrolDirection;
    private int stepsTaken;
    private boolean inCombat;
    private int turnsSinceLastTeleport;
    private boolean isActive;

    public MonsterAiState() {
        this.patrolDirection = null;
        this.stepsTaken = 0;
        this.inCombat = false;
        this.turnsSinceLastTeleport = 0;
        this.isActive = true;
    }

    public Direction getPatrolDirection() {
        return patrolDirection;
    }

    public void setPatrolDirection(Direction dir) {
        this.patrolDirection = dir;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public void setStepsTaken(int steps) {
        this.stepsTaken = steps;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void setInCombat(boolean combat) {
        this.inCombat = combat;
    }

    public int getTurnsSinceLastTeleport() {
        return turnsSinceLastTeleport;
    }

    public void incrementTurnsSinceLastTeleport() {
        turnsSinceLastTeleport++;
    }

    public void resetTurnsSinceLastTeleport() {
        turnsSinceLastTeleport = 0;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
