package org.example.domain.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Класс, представляющий коридор, соединяющий комнаты. Коридор состоит из последовательности клеток
 * и может иметь повороты.
 */
public class Corridor {
    private final List<Position> tiles; // Все клетки коридора
    private final Room roomA; // Первая комната
    private final Room roomB; // Вторая комната
    private final Position connectionA; // Точка соединения с первой комнатой
    private final Position connectionB; // Точка соединения со второй комнатой
    private final CorridorType type; // Тип коридора (прямой, L-образный и т.д.)

    public enum CorridorType {
        STRAIGHT_HORIZONTAL, // Прямой горизонтальный
        STRAIGHT_VERTICAL, // Прямой вертикальный
        L_SHAPE, // Г-образный (один поворот)
        Z_SHAPE, // Z-образный (два поворота)
        COMPLEX // Сложный (более двух поворотов)
    }

    public Corridor(
            Room roomA, Room roomB, Position connectionA, Position connectionB, List<Position> tiles) {
        this.roomA = roomA;
        this.roomB = roomB;
        this.connectionA = connectionA;
        this.connectionB = connectionB;
        this.tiles = new ArrayList<>(tiles);
        this.type = determineType(tiles);
    }

    private CorridorType determineType(List<Position> tiles) {
        if (tiles.isEmpty()) return CorridorType.COMPLEX;

        // Проверяем, все ли точки на одной линии по X или Y
        boolean sameX = tiles.stream().allMatch(p -> p.x() == tiles.getFirst().x());
        boolean sameY = tiles.stream().allMatch(p -> p.y() == tiles.getFirst().y());

        if (sameX) return CorridorType.STRAIGHT_VERTICAL;
        if (sameY) return CorridorType.STRAIGHT_HORIZONTAL;

        // Считаем количество изменений направления
        int directionChanges = countDirectionChanges(tiles);

        if (directionChanges == 1) return CorridorType.L_SHAPE;
        if (directionChanges == 2) return CorridorType.Z_SHAPE;
        return CorridorType.COMPLEX;
    }

    private int countDirectionChanges(List<Position> tiles) {
        if (tiles.size() < 3) return 0;

        int changes = 0;
        Integer lastDx = null;
        Integer lastDy = null;

        for (int i = 1; i < tiles.size(); i++) {
            Position prev = tiles.get(i - 1);
            Position curr = tiles.get(i);

            int dx = Integer.compare(curr.x(), prev.x());
            int dy = Integer.compare(curr.y(), prev.y());

            if (lastDx != null && lastDy != null) {
                if (dx != lastDx || dy != lastDy) {
                    changes++;
                }
            }

            lastDx = dx;
            lastDy = dy;
        }

        return changes;
    }

    /** Получить все клетки коридора. */
    public List<Position> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    /** Получить длину коридора (количество клеток). */
    public int getLength() {
        return tiles.size();
    }

    /** Получить первую комнату. */
    public Room getRoomA() {
        return roomA;
    }

    /** Получить вторую комнату. */
    public Room getRoomB() {
        return roomB;
    }

    /** Получить точку соединения с первой комнатой. */
    public Position getConnectionA() {
        return connectionA;
    }

    /** Получить точку соединения со второй комнатой. */
    public Position getConnectionB() {
        return connectionB;
    }

    /** Получить тип коридора. */
    public CorridorType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format(
                "Corridor[%s: %s -> %s, length=%d, type=%s]",
                roomA != null ? "R" + roomA.hashCode() : "?", connectionA, connectionB, tiles.size(), type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Corridor corridor = (Corridor) o;
        return Objects.equals(roomA, corridor.roomA)
                && Objects.equals(roomB, corridor.roomB)
                && Objects.equals(connectionA, corridor.connectionA)
                && Objects.equals(connectionB, corridor.connectionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomA, roomB, connectionA, connectionB);
    }
}
