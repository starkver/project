package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/** DTO для уровня. Содержит полную информацию о структуре уровня, включая двери. */
public class LevelDto {
    private int levelNumber;
    private List<RoomDto> rooms;
    private List<CorridorDto> corridors;
    private List<DoorDto> doors; // Двери на уровне

    // Позиция выхода на карте
    private int exitTileX = -1;
    private int exitTileY = -1;

    // Позиции комнат в сетке 3x3
    private int startRoomRow = -1;
    private int startRoomCol = -1;
    private int exitRoomRow = -1;
    private int exitRoomCol = -1;

    public LevelDto(int levelNumber) {
        this.levelNumber = levelNumber;
        this.rooms = new ArrayList<>();
        this.corridors = new ArrayList<>();
        this.doors = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getLevelNumber() {
        return levelNumber;
    }

    public List<RoomDto> getRooms() {
        return rooms;
    }

    public List<CorridorDto> getCorridors() {
        return corridors;
    }

    public List<DoorDto> getDoors() {
        return doors;
    }

    public int getExitTileX() {
        return exitTileX;
    }

    public void setExitTileX(int exitTileX) {
        this.exitTileX = exitTileX;
    }

    public int getExitTileY() {
        return exitTileY;
    }

    public void setExitTileY(int exitTileY) {
        this.exitTileY = exitTileY;
    }

    public int getStartRoomRow() {
        return startRoomRow;
    }

    public void setStartRoomRow(int startRoomRow) {
        this.startRoomRow = startRoomRow;
    }

    public int getStartRoomCol() {
        return startRoomCol;
    }

    public void setStartRoomCol(int startRoomCol) {
        this.startRoomCol = startRoomCol;
    }

    public int getExitRoomRow() {
        return exitRoomRow;
    }

    public void setExitRoomRow(int exitRoomRow) {
        this.exitRoomRow = exitRoomRow;
    }

    public int getExitRoomCol() {
        return exitRoomCol;
    }

    public void setExitRoomCol(int exitRoomCol) {
        this.exitRoomCol = exitRoomCol;
    }

    // Вспомогательные методы
    public void addRoom(RoomDto room) {
        rooms.add(room);
    }

    public void addCorridor(CorridorDto corridor) {
        corridors.add(corridor);
    }

    public void addDoor(DoorDto door) {
        doors.add(door);
    }
}
