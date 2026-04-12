package org.example.domain.service;

import org.example.domain.model.enums.*;

/**
 * Результат боевого взаимодействия. Содержит информацию о попадании, уроне и специальных эффектах.
 */
public class CombatResult {
    private final boolean hit;
    private final int damage;
    private final boolean monsterDefeated;
    private final String specialEffect;
    private final int playerHealthAfter;
    private final int monsterHealthAfter;
    private final MonsterType monsterType;

    public CombatResult(
            boolean hit,
            int damage,
            boolean monsterDefeated,
            String specialEffect,
            int playerHealthAfter,
            int monsterHealthAfter,
            MonsterType monsterType) {
        this.hit = hit;
        this.damage = damage;
        this.monsterDefeated = monsterDefeated;
        this.specialEffect = specialEffect;
        this.playerHealthAfter = playerHealthAfter;
        this.monsterHealthAfter = monsterHealthAfter;
        this.monsterType = monsterType;
    }

    // Базовый конструктор для обратной совместимости
    public CombatResult(boolean hit, int damage, boolean monsterDefeated, String specialEffect) {
        this(hit, damage, monsterDefeated, specialEffect, 0, 0, null);
    }

    // Геттеры
    public boolean isHit() {
        return hit;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isMonsterDefeated() {
        return monsterDefeated;
    }

    public String getSpecialEffect() {
        return specialEffect;
    }

    public int getPlayerHealthAfter() {
        return playerHealthAfter;
    }

    public int getMonsterHealthAfter() {
        return monsterHealthAfter;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    /** Проверяет, был ли специальный эффект от атаки (вампиризм, сон и т.д.) */
    public boolean hasSpecialEffect() {
        return specialEffect != null && !specialEffect.isEmpty();
    }

    /** Возвращает сообщение для отображения в интерфейсе */
    public String getMessage() {
        if (!hit) {
            return "Промах!";
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Попадание! Урон: ").append(damage);

        if (hasSpecialEffect()) {
            msg.append(". ").append(specialEffect);
        }

        if (monsterDefeated) {
            msg.append(". Враг повержен!");
        }

        return msg.toString();
    }
}
