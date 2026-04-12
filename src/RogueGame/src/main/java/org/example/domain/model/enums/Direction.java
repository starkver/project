package org.example.domain.model.enums;

/**
 * Направления движения в игре. Включает как основные направления (WASD), так и диагональные (для
 * змея-мага).
 */
public enum Direction {

    // Основные направления (для игрока и большинства монстров)
    UP(0, -1, "вверх", 'w', 'W'),
    DOWN(0, 1, "вниз", 's', 'S'),
    LEFT(-1, 0, "влево", 'a', 'A'),
    RIGHT(1, 0, "вправо", 'd', 'D'),

    // Диагональные направления (для змея-мага)
    UP_LEFT(-1, -1, "вверх-влево", 'q', 'Q'),
    UP_RIGHT(1, -1, "вверх-вправо", 'e', 'E'),
    DOWN_LEFT(-1, 1, "вниз-влево", 'z', 'Z'),
    DOWN_RIGHT(1, 1, "вниз-вправо", 'c', 'C'),

    // Специальные направления
    NONE(0, 0, "ничего", '\0', '\0'), // Для случаев, когда движение не требуется
    WAIT(0, 0, "ожидание", '.', '.'), // Пропуск хода (игрок ничего не делает)

    // Добавлено: состояние для "усыпленного" игрока
    SLEEP(0, 0, "сон", '\0', '\0'); // Игрок спит и не может двигаться

    private final int dx;
    private final int dy;
    private final String displayName;
    private final char keyChar;
    private final char alternateKeyChar;

    Direction(int dx, int dy, String displayName, char keyChar, char alternateKeyChar) {
        this.dx = dx;
        this.dy = dy;
        this.displayName = displayName;
        this.keyChar = keyChar;
        this.alternateKeyChar = alternateKeyChar;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public char getKeyChar() {
        return keyChar;
    }

    public char getAlternateKeyChar() {
        return alternateKeyChar;
    }

    /** Проверяет, является ли направление "пустым" (NONE, WAIT или SLEEP). */
    public boolean isNone() {
        return this == NONE || this == WAIT || this == SLEEP;
    }

    /**
     * Проверяет, может ли игрок использовать это направление для движения. SLEEP и NONE не могут быть
     * использованы для движения.
     */
    public boolean isMovable() {
        return this != NONE && this != WAIT && this != SLEEP;
    }

    /** Получить противоположное направление. */
    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case UP_LEFT -> DOWN_RIGHT;
            case UP_RIGHT -> DOWN_LEFT;
            case DOWN_LEFT -> UP_RIGHT;
            case DOWN_RIGHT -> UP_LEFT;
            case NONE -> NONE;
            case WAIT -> WAIT;
            case SLEEP -> SLEEP;
            default -> NONE;
        };
    }

    /** Повернуть направление на 90 градусов по часовой стрелке. */
    public Direction rotateClockwise() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
            case UP_LEFT -> UP_RIGHT;
            case UP_RIGHT -> DOWN_RIGHT;
            case DOWN_RIGHT -> DOWN_LEFT;
            case DOWN_LEFT -> UP_LEFT;
            case NONE -> NONE;
            case WAIT -> WAIT;
            case SLEEP -> SLEEP;
            default -> NONE;
        };
    }

    /** Получить направление из смещения координат. */
    public static Direction fromDelta(int dx, int dy) {
        // Нормализуем значения к -1, 0, 1
        dx = Integer.compare(dx, 0);
        dy = Integer.compare(dy, 0);

        for (Direction dir : values()) {
            if (dir.dx == dx && dir.dy == dy && dir.isMovable()) {
                return dir;
            }
        }
        return NONE;
    }

    /** Получить все основные направления (WASD). */
    public static Direction[] cardinalDirections() {
        return new Direction[] {UP, DOWN, LEFT, RIGHT};
    }

    /** Получить все диагональные направления. */
    public static Direction[] diagonalDirections() {
        return new Direction[] {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    }

    /** Получить все направления, которые можно использовать для движения. */
    public static Direction[] movableDirections() {
        return new Direction[] {UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    }

    /** Получить случайное основное направление. */
    public static Direction randomCardinal() {
        Direction[] cardinals = cardinalDirections();
        return cardinals[(int) (Math.random() * cardinals.length)];
    }

    /** Получить случайное диагональное направление. */
    public static Direction randomDiagonal() {
        Direction[] diagonals = diagonalDirections();
        return diagonals[(int) (Math.random() * diagonals.length)];
    }

    @Override
    public String toString() {
        return displayName;
    }
}
