package org.example.domain.model.enums;

/** Тип клетки карты. Определяет проходимость и отображение */
public enum TileType {
    WALL('#', false), // Стена: нельзя пройти
    FLOOR('.', true), // Пол комнаты: можно пройти
    CORRIDOR('+', true), // Коридор: можно пройти
    EXIT('>', true), // Лестница: переход на следующий уровень
    DOOR(
            '+',
            true); // Дверь: можно пройти, если есть ключ (по умолчанию проходима, но логика проверки в
    // DoorService)

    private final char symbol;
    private final boolean walkable;

    TileType(char symbol, boolean walkable) {
        this.symbol = symbol;
        this.walkable = walkable;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
