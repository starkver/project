package org.example.domain.service;

import org.example.domain.model.entity.*;
import org.example.domain.model.enums.*;

/**
 * Результат перемещения игрока или монстра. Содержит информацию о том, что произошло в результате
 * хода.
 */
public class MoveResult {
    private final MoveResultType type;
    private final Object data;
    private final String message;
    private final CombatResult combatResult;
    private final Monster monster;
    private final Item item;

    // Конструктор для простого результата (MOVE, BLOCKED, NEXT_LEVEL)
    public MoveResult(MoveResultType type, String message) {
        this(type, message, null, null, null, null);
    }

    // Конструктор для подбора предмета (PICKUP)
    public MoveResult(MoveResultType type, Item item) {
        this.type = type;
        this.message = "Подобран: " + (item != null ? item.getName() : "неизвестный предмет");
        this.data = item;
        this.combatResult = null;
        this.monster = null;
        this.item = item;
    }

    // Конструктор для результата боя (ATTACK_HIT, ATTACK_KILL, MONSTER_ATTACK)
    public MoveResult(MoveResultType type, CombatResult combatResult, Monster monster) {
        this.type = type;
        this.message = combatResult != null ? combatResult.getMessage() : "";
        this.data = null;
        this.combatResult = combatResult;
        this.monster = monster;
        this.item = null;
    }

    // Конструктор для перемещения монстра (MONSTER_MOVE, MONSTER_BLOCKED)
    public MoveResult(MoveResultType type, String message, Monster monster) {
        this.type = type;
        this.message = message;
        this.data = null;
        this.combatResult = null;
        this.monster = monster;
        this.item = null;
    }

    // Основной конструктор (для других случаев)
    public MoveResult(
            MoveResultType type,
            String message,
            Object data,
            CombatResult combatResult,
            Monster monster,
            Item item) {
        this.type = type;
        this.message = message;
        this.data = data;
        this.combatResult = combatResult;
        this.monster = monster;
        this.item = item;
    }

    // Геттеры
    public MoveResultType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public Monster getMonster() {
        return monster;
    }

    public Item getItem() {
        return item;
    }
}
