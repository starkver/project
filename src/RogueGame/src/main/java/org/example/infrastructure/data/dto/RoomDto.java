package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/** DTO для комнаты. Содержит полную информацию о комнате, включая монстров и предметы. */
public class RoomDto {
    // Основная информация
    private int id; // уникальный ID комнаты (0-8)
    private int row; // строка в сетке 3x3
    private int col; // столбец в сетке 3x3
    private int topLeftX; // X координата левого верхнего угла
    private int topLeftY; // Y координата левого верхнего угла
    private int width; // ширина комнаты
    private int height; // высота комнаты

    // Флаги
    private boolean isStartRoom;
    private boolean isExitRoom;

    // Дверные проемы
    private List<PositionDto> doorways;

    // Содержимое комнаты
    private List<MonsterDto> monsters;
    private List<ItemDto> items;

    public RoomDto(int id, int row, int col, int topLeftX, int topLeftY, int width, int height) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
        this.doorways = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getTopLeftX() {
        return topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isStartRoom() {
        return isStartRoom;
    }

    public void setStartRoom(boolean startRoom) {
        isStartRoom = startRoom;
    }

    public boolean isExitRoom() {
        return isExitRoom;
    }

    public void setExitRoom(boolean exitRoom) {
        isExitRoom = exitRoom;
    }

    public List<PositionDto> getDoorways() {
        return doorways;
    }

    public List<MonsterDto> getMonsters() {
        return monsters;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    // Вспомогательные методы
    public void addDoorway(int x, int y) {
        doorways.add(new PositionDto(x, y));
    }

    public void addMonster(MonsterDto monster) {
        monsters.add(monster);
    }

    public void addItem(ItemDto item) {
        items.add(item);
    }
}
