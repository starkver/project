package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/** DTO для коридора. Содержит ID комнат для восстановления связей. */
public class CorridorDto {
    private int fromRoomId; // ID первой комнаты
    private int toRoomId; // ID второй комнаты

    private PositionDto connectionA; // точка соединения с первой комнатой
    private PositionDto connectionB; // точка соединения со второй комнатой

    private List<PositionDto> tiles; // список всех клеток коридора
    private String type; // тип: STRAIGHT_HORIZONTAL, L_SHAPE, etc.

    public CorridorDto(int fromRoomId, int toRoomId) {
        this.fromRoomId = fromRoomId;
        this.toRoomId = toRoomId;
        this.tiles = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getFromRoomId() {
        return fromRoomId;
    }

    public int getToRoomId() {
        return toRoomId;
    }

    public PositionDto getConnectionA() {
        return connectionA;
    }

    public void setConnectionA(PositionDto connectionA) {
        this.connectionA = connectionA;
    }

    public PositionDto getConnectionB() {
        return connectionB;
    }

    public void setConnectionB(PositionDto connectionB) {
        this.connectionB = connectionB;
    }

    public List<PositionDto> getTiles() {
        return tiles;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Вспомогательные методы
    public void addTile(int x, int y) {
        tiles.add(new PositionDto(x, y));
    }

    public int getLength() {
        return tiles.size();
    }
}
