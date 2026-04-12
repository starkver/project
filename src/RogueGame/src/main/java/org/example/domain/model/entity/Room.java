package org.example.domain.model.entity;

import java.util.*;

/**
 * Класс, представляющий комнату на уровне. Комната имеет прямоугольную форму с координатами
 * верхнего левого угла, шириной и высотой. Содержит свои стены и пол.
 */
public class Room {
    private final Position topLeft; // Верхний левый угол комнаты (включая стены)
    private final int width; // Ширина комнаты (включая стены)
    private final int height; // Высота комнаты (включая стены)

    // Внутренняя область комнаты (где можно ходить, без стен)
    private final int interiorX; // X координата начала внутренней области
    private final int interiorY; // Y координата начала внутренней области
    private final int interiorWidth; // Ширина внутренней области
    private final int interiorHeight; // Высота внутренней области

    // Список позиций дверей/проходов в стенах комнаты
    private final List<Position> doorways;

    // Является ли комната стартовой
    private boolean isStartRoom;

    // Является ли комната комнатой с выходом
    private boolean isExitRoom;

    /**
     * Конструктор комнаты.
     *
     * @param topLeft верхний левый угол (включая стены)
     * @param width ширина комнаты (включая стены, минимум 3)
     * @param height высота комнаты (включая стены, минимум 3)
     */
    public Room(Position topLeft, int width, int height) {
        if (width < 3 || height < 3) {
            throw new IllegalArgumentException("Комната должна быть минимум 3x3 (с учётом стен)");
        }

        this.topLeft = topLeft;
        this.width = width;
        this.height = height;

        // Внутренняя область (без стен) - отступаем по 1 клетке от краёв
        this.interiorX = topLeft.x() + 1;
        this.interiorY = topLeft.y() + 1;
        this.interiorWidth = width - 2;
        this.interiorHeight = height - 2;

        this.doorways = new ArrayList<>();
        this.isStartRoom = false;
        this.isExitRoom = false;
    }

    /** Проверить, принадлежат ли координаты комнате (включая стены). */
    public boolean contains(int x, int y) {
        return x >= topLeft.x()
                && x < topLeft.x() + width
                && y >= topLeft.y()
                && y < topLeft.y() + height;
    }

    /** Проверить, принадлежат ли координаты комнате (включая стены). */
    public boolean contains(Position pos) {
        return contains(pos.x(), pos.y());
    }

    /** Проверить, находится ли точка внутри комнаты (не на стенах). */
    public boolean containsInterior(int x, int y) {
        return x >= interiorX
                && x < interiorX + interiorWidth
                && y >= interiorY
                && y < interiorY + interiorHeight;
    }

    /** Проверить, является ли точка стеной комнаты. */
    public boolean isWall(int x, int y) {
        return contains(x, y) && !containsInterior(x, y);
    }

    /**
     * Добавить дверной проём.
     *
     * @param x, y координаты проёма (должны быть на стене комнаты)
     */
    public void addDoorway(int x, int y) {
        if (!isWall(x, y)) {
            return; // Можно ставить только на стенах
        }

        Position doorway = new Position(x, y);
        if (!doorways.contains(doorway)) {
            doorways.add(doorway);
        }
    }

    /** Добавить дверной проём. */
    public void addDoorway(Position pos) {
        addDoorway(pos.x(), pos.y());
    }

    /** Получить все дверные проёмы комнаты. */
    public List<Position> getDoorways() {
        return Collections.unmodifiableList(doorways);
    }

    /** Получить все внутренние позиции комнаты (где можно ходить). */
    public List<Position> getAllInteriorPositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = interiorY; y < interiorY + interiorHeight; y++) {
            for (int x = interiorX; x < interiorX + interiorWidth; x++) {
                positions.add(new Position(x, y));
            }
        }
        return positions;
    }

    /** Получить случайную внутреннюю позицию. */
    public Position getRandomInteriorPosition(Random random) {
        int x = interiorX + random.nextInt(interiorWidth);
        int y = interiorY + random.nextInt(interiorHeight);
        return new Position(x, y);
    }

    public Position getTopLeft() {
        return topLeft;
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

    /** Получить центр комнаты. */
    public Position getCenter() {
        int centerX = topLeft.x() + width / 2;
        int centerY = topLeft.y() + height / 2;
        return new Position(centerX, centerY);
    }

    @Override
    public String toString() {
        return String.format(
                "Room[%d,%d %dx%d, interior=%d,%d %dx%d, doorways=%d]",
                topLeft.x(),
                topLeft.y(),
                width,
                height,
                interiorX,
                interiorY,
                interiorWidth,
                interiorHeight,
                doorways.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return width == room.width && height == room.height && topLeft.equals(room.topLeft);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLeft, width, height);
    }
}
