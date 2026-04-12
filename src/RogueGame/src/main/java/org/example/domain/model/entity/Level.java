package org.example.domain.model.entity;

import java.util.*;
import org.example.domain.model.enums.TileType;

/**
 * Класс, представляющий игровой уровень. Содержит комнаты, коридоры, карту тайлов и информацию об
 * уровне.
 */
public class Level {
    private final int levelNumber;

    // Карта тайлов (основное хранилище всех клеток)
    private final TileMap tileMap;

    // Комнаты на уровне (сетка 3x3)
    private final Room[][] rooms;
    private static final int ROOMS_PER_ROW = 3;

    // Коридоры, соединяющие комнаты
    private final List<Corridor> corridors;

    // Двери на уровне
    private final List<Door> doors;

    // Позиции ключевых комнат
    private Position startRoomPosition; // координаты стартовой комнаты в сетке комнат
    private Position exitRoomPosition; // координаты комнаты с выходом в сетке комнат

    // Позиция выхода на карте (Tile)
    private Position exitTilePosition;

    // Список всех монстров на уровне (для быстрого доступа)
    private final List<Monster> monsters;

    // Список всех предметов на уровне (для быстрого доступа)
    private final List<Item> items;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.tileMap = new TileMap();
        this.rooms = new Room[ROOMS_PER_ROW][ROOMS_PER_ROW];
        this.corridors = new ArrayList<>();
        this.doors = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    /**
     * Получить комнату по индексам в сетке.
     *
     * @param row индекс строки (0-2)
     * @param col индекс столбца (0-2)
     * @return комната или null
     */
    public Room getRoom(int row, int col) {
        if (row < 0 || row >= ROOMS_PER_ROW || col < 0 || col >= ROOMS_PER_ROW) {
            return null;
        }
        return rooms[row][col];
    }

    /** Установить комнату по индексам. */
    public void setRoom(int row, int col, Room room) {
        if (row >= 0 && row < ROOMS_PER_ROW && col >= 0 && col < ROOMS_PER_ROW) {
            rooms[row][col] = room;
        }
    }

    public List<Corridor> getCorridors() {
        return Collections.unmodifiableList(corridors);
    }

    public void addCorridor(Corridor corridor) {
        corridors.add(corridor);
    }

    public List<Door> getDoors() {
        return Collections.unmodifiableList(doors);
    }

    public void addDoor(Door door) {
        if (door != null && !doors.contains(door)) {
            doors.add(door);
            // Устанавливаем тип клетки на карте как дверь
            tileMap.setTileType(door.getX(), door.getY(), TileType.DOOR);
        }
    }

    /** Получить дверь по позиции. */
    public Door getDoorAt(int x, int y) {
        return doors.stream()
                .filter(door -> door.getX() == x && door.getY() == y)
                .findFirst()
                .orElse(null);
    }

    /** Проверить, есть ли дверь в указанной позиции. */
    public boolean isDoor(int x, int y) {
        return doors.stream().anyMatch(door -> door.getX() == x && door.getY() == y);
    }

    public Position getStartRoomPosition() {
        return startRoomPosition;
    }

    public void setStartRoomPosition(Position startRoomPosition) {
        this.startRoomPosition = startRoomPosition;
    }

    public Position getExitRoomPosition() {
        return exitRoomPosition;
    }

    public void setExitRoomPosition(Position exitRoomPosition) {
        this.exitRoomPosition = exitRoomPosition;
    }

    public Position getExitTilePosition() {
        return exitTilePosition;
    }

    public void setExitTilePosition(Position exitTilePosition) {
        this.exitTilePosition = exitTilePosition;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Добавить монстра на уровень. */
    public void addMonster(Monster monster) {
        if (monster != null && !monsters.contains(monster)) {
            monsters.add(monster);
            // Также устанавливаем монстра на тайл
            tileMap.setMonster(monster.getX(), monster.getY(), monster);
        }
    }

    /** Удалить монстра с уровня. */
    public void removeMonster(Monster monster) {
        if (monster != null) {
            monsters.remove(monster);
            // Убираем монстра с тайла
            tileMap.setMonster(monster.getX(), monster.getY(), null);
        }
    }

    /** Получить всех живых монстров. */
    public List<Monster> getAliveMonsters() {
        return monsters.stream().filter(Monster::isAlive).toList();
    }

    /** Получить монстра по позиции. */
    public Monster getMonsterAt(int x, int y) {
        return tileMap.getMonster(x, y);
    }

    // ============================================================
    // Управление предметами
    // ============================================================

    /** Добавить предмет на уровень. */
    public void addItem(Item item, int x, int y) {
        if (item != null) {
            items.add(item);
            tileMap.setItem(x, y, item);
        }
    }

    /** Удалить предмет с уровня. */
    public void removeItem(Item item) {
        if (item != null) {
            items.remove(item);
            // Ищем и убираем предмет со всех тайлов
            for (int y = 0; y < tileMap.getHeight(); y++) {
                for (int x = 0; x < tileMap.getWidth(); x++) {
                    if (tileMap.getItem(x, y) == item) {
                        tileMap.setItem(x, y, null);
                        return;
                    }
                }
            }
        }
    }

    /** Получить предмет по позиции. */
    public Item getItemAt(int x, int y) {
        return tileMap.getItem(x, y);
    }

    /** Проверить, является ли позиция выходом на следующий уровень. */
    public boolean isExit(int x, int y) {
        return exitTilePosition != null && exitTilePosition.x() == x && exitTilePosition.y() == y;
    }

    /** Найти комнату, содержащую указанную позицию. */
    public Room findRoomContaining(int x, int y) {
        for (int row = 0; row < ROOMS_PER_ROW; row++) {
            for (int col = 0; col < ROOMS_PER_ROW; col++) {
                Room room = rooms[row][col];
                if (room != null && room.contains(x, y)) {
                    return room;
                }
            }
        }
        return null;
    }

    /** Получить количество живых монстров. */
    public int getMonsterCount() {
        return (int) monsters.stream().filter(Monster::isAlive).count();
    }

    /** Получить количество комнат. */
    public int getRoomCount() {
        int count = 0;
        for (int row = 0; row < ROOMS_PER_ROW; row++) {
            for (int col = 0; col < ROOMS_PER_ROW; col++) {
                if (rooms[row][col] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Получить количество дверей. */
    public int getDoorCount() {
        return doors.size();
    }

    @Override
    public String toString() {
        return String.format(
                "Уровень %d: %d комнат, %d коридоров, %d дверей, %d монстров, %d предметов",
                levelNumber,
                getRoomCount(),
                corridors.size(),
                getDoorCount(),
                getMonsterCount(),
                items.size());
    }
}
