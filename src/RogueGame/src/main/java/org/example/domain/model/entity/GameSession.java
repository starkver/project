package org.example.domain.model.entity;

import org.example.domain.model.enums.GameState;

/**
 * Класс, представляющий игровую сессию. Объединяет игрока, текущий уровень, статистику и состояние
 * игры.
 */
public class GameSession {
    private final long sessionId; // Уникальный идентификатор сессии
    private final Player player; // Игрок
    private final GameStatistics statistics; // Статистика текущей сессии
    private Backpack backpack;

    private Level currentLevel; // Текущий уровень
    private int currentLevelNumber; // Номер текущего уровня (1-21)
    private GameState gameState; // Состояние игры

    /** Конструктор для новой игровой сессии. */
    public GameSession(
            long sessionId,
            Player player,
            Level currentLevel,
            int currentLevelNumber,
            GameState gameState,
            GameStatistics statistics) {
        this.sessionId = sessionId;
        this.player = player;
        this.currentLevel = currentLevel;
        this.currentLevelNumber = currentLevelNumber;
        this.gameState = gameState;
        this.statistics = statistics;
    }

    public long getSessionId() {
        return sessionId;
    }

    public Player getPlayer() {
        return player;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameStatistics getStatistics() {
        return statistics;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public void setCurrentLevel(Level currentLevel) {
        if (currentLevel == null) {
            throw new IllegalArgumentException("Уровень не может быть null");
        }
        this.currentLevel = currentLevel;
    }

    public void setCurrentLevelNumber(int currentLevelNumber) {
        if (currentLevelNumber < 1 || currentLevelNumber > 21) {
            throw new IllegalArgumentException("Номер уровня должен быть от 1 до 21");
        }
        this.currentLevelNumber = currentLevelNumber;
    }

    public void setGameState(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("Состояние игры не может быть null");
        }
        this.gameState = gameState;
    }

    /** Проверяет, активна ли игра (игрок может действовать). */
    public boolean isActive() {
        boolean active = gameState == GameState.PLAYING;
        return active;
    }

    /** Проверяет, обрабатываются ли ходы монстров. */
    public boolean shouldProcessMonsters() {
        return gameState == GameState.PLAYING
                || gameState == GameState.WAITING
                || gameState == GameState.SLEEPING;
    }

    /** Завершает игру смертью игрока. */
    public void gameOver() {
        this.gameState = GameState.GAME_OVER;
    }

    @Override
    public String toString() {
        return String.format(
                "GameSession[id=%d, level=%d, state=%s, player HP=%d/%d]",
                sessionId, currentLevelNumber, gameState, player.getHealth(), player.getMaxHealth());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSession that = (GameSession) o;
        return sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(sessionId);
    }

    public boolean isVictory() {
        return gameState == GameState.VICTORY;
    }
}
