package org.example.infrastructure.data.dto;

/** DTO для предмета. Использует String для ItemType и ItemSubType для безопасной сериализации. */
public class ItemDto {
    private String type; // FOOD, ELIXIR, SCROLL, WEAPON, TREASURE
    private String subType; // BREAD, MEAT, ELIXIR_STRENGTH, SHORT_SWORD и т.д.
    private int value;
    private String name;

    // Позиция для предметов на полу
    private int x;
    private int y;

    public ItemDto(String type, String subType, int value, String name, int x, int y) {
        this.type = type;
        this.subType = subType;
        this.value = value;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
