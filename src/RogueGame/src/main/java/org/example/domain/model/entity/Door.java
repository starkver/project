package org.example.domain.model.entity;

import org.example.domain.model.enums.KeyColor;

/** Сущность двери. Содержит позицию, цвет и состояние (заперта/открыта). */
public class Door {
    private final int x;
    private final int y;
    private final KeyColor color;
    private boolean isOpen;

    /**
     * Создаёт новую запертую дверь.
     *
     * @param x координата X на карте
     * @param y координата Y на карте
     * @param color цвет двери (определяет, какой ключ подходит)
     */
    public Door(int x, int y, KeyColor color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.isOpen = false;
    }

    /** Возвращает координату X двери. */
    public int getX() {
        return x;
    }

    /** Возвращает координату Y двери. */
    public int getY() {
        return y;
    }

    /** Возвращает цвет двери. */
    public KeyColor getColor() {
        return color;
    }

    /** Проверяет, открыта ли дверь. */
    public boolean isOpen() {
        return isOpen;
    }

    /** Проверяет, заперта ли дверь. */
    public boolean isLocked() {
        return !isOpen;
    }

    /** Открывает дверь. */
    public void open() {
        if (isOpen) {
            return;
        }
        this.isOpen = true;
    }

    @Override
    public String toString() {
        return String.format(
                "Door[%d,%d, %s, %s]", x, y, color.getDisplayName(), isOpen ? "open" : "locked");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Door door = (Door) o;
        return x == door.x && y == door.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
