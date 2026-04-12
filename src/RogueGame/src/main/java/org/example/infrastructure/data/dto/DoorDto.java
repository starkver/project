package org.example.infrastructure.data.dto;

/**
 * DTO для двери. Содержит позицию, цвет и состояние двери. Используется для сохранения и загрузки
 * состояния дверей.
 */
public class DoorDto {
    private int x;
    private int y;
    private String color; // RED, BLUE, YELLOW, GREEN, PURPLE
    private boolean isOpen;

    public DoorDto(int x, int y, String color, boolean isOpen) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.isOpen = isOpen;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return String.format("DoorDto[%d,%d, %s, %s]", x, y, color, isOpen ? "открыто" : "закрыто");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoorDto doorDto = (DoorDto) o;
        return x == doorDto.x && y == doorDto.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
