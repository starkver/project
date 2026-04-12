package org.example.domain.model.entity;

import org.example.domain.model.enums.StatType;

/** Временный эффект от эликсира. Действует указанное количество игровых ходов. */
public record TemporaryEffect(
        String id, // уникальный ID
        StatType stat, // какую характеристику меняет
        int bonusValue, // на сколько изменяет
        int durationTurns, // сколько ходов действует
        int appliedAtTurn // номер хода применения
) {
    // Активен ли эффект на текущем ходу?
    public boolean isActive(int currentTurn) {
        return currentTurn < appliedAtTurn + durationTurns;
    }
}
