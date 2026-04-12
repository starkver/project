package org.example.infrastructure.data.dto;

/**
 * DTO для временного эффекта (эликсир). Использует String для StatType для безопасной сериализации.
 */
public class TemporaryEffectDto {
    private String id;
    private String stat; // STRENGTH, AGILITY, MAX_HEALTH
    private int bonusValue;
    private int durationTurns;
    private int appliedAtTurn;

    public TemporaryEffectDto(
            String id, String stat, int bonusValue, int durationTurns, int appliedAtTurn) {
        this.id = id;
        this.stat = stat;
        this.bonusValue = bonusValue;
        this.durationTurns = durationTurns;
        this.appliedAtTurn = appliedAtTurn;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStat() {
        return stat;
    }

    public int getBonusValue() {
        return bonusValue;
    }

    public int getDurationTurns() {
        return durationTurns;
    }

    public int getAppliedAtTurn() {
        return appliedAtTurn;
    }
}
