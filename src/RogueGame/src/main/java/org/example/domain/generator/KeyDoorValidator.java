package org.example.domain.generator;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.KeyColor;

/**
 * Валидатор доступности ключей и дверей. Использует BFS для проверки, что все ключи достижимы до
 * дверей, и нет софтлоков (ситуаций, когда игрок не может пройти дальше).
 */
public class KeyDoorValidator {

    /**
     * Проверяет валидность генерации ключей и дверей на уровне.
     *
     * @param level уровень
     * @param startPosition стартовая позиция игрока
     * @return true если валидация пройдена
     */
    public boolean validate(Level level, Position startPosition) {
        List<Door> doors = level.getDoors();

        if (doors.isEmpty()) {
            return true;
        }

        // Собираем информацию о ключах на уровне
        Map<KeyColor, List<Position>> keyPositions = collectKeyPositions(level);

        // Для каждой двери проверяем достижимость ключа
        for (Door door : doors) {
            Position doorPos = new Position(door.getX(), door.getY());
            KeyColor doorColor = door.getColor();

            // Если дверь уже открыта, пропускаем
            if (door.isOpen()) {
                continue;
            }

            // Если нет ключа этого цвета на уровне, валидация не пройдена
            if (!keyPositions.containsKey(doorColor) || keyPositions.get(doorColor).isEmpty()) {
                System.out.println("Нет ключа для двери цвета " + doorColor);
                return false;
            }

            // Проверяем, достижим ли хотя бы один ключ этого цвета до двери
            if (!isKeyReachable(level, startPosition, door, doorColor, keyPositions.get(doorColor))) {
                System.out.println("Невозможно подобрать ключ для двери цвета " + doorColor);
                return false;
            }
        }

        return true;
    }

    /** Собирает позиции всех ключей на уровне. */
    private Map<KeyColor, List<Position>> collectKeyPositions(Level level) {
        Map<KeyColor, List<Position>> keyPositions = new HashMap<>();

        for (Item item : level.getItems()) {
            if (item instanceof Key) {
                Key key = (Key) item;
                keyPositions
                        .computeIfAbsent(key.getColor(), k -> new ArrayList<>())
                        .add(new Position(key.getX(), key.getY()));
            }
        }

        return keyPositions;
    }

    /** Проверяет, достижим ли ключ до двери. */
    private boolean isKeyReachable(
            Level level, Position start, Door door, KeyColor color, List<Position> keyPositions) {
        Position doorPos = new Position(door.getX(), door.getY());

        // Сначала проверяем, достижима ли дверь из стартовой позиции
        if (!isReachableWithoutDoors(level, start, doorPos, null)) {
            // Если дверь недостижима без ключей, она нам не нужна
            return true;
        }

        // Проверяем, достижим ли хотя бы один ключ этого цвета
        for (Position keyPos : keyPositions) {
            // Проверяем, достижим ли ключ из стартовой позиции
            if (isReachableWithoutDoors(level, start, keyPos, null)) {
                // Проверяем, достижима ли дверь из ключа (без учёта дверей этого цвета)
                if (isReachableWithoutDoors(level, keyPos, doorPos, color)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * BFS-проверка достижимости между двумя точками.
     *
     * @param level уровень
     * @param from начальная позиция
     * @param to целевая позиция
     * @param ignoreDoorColor цвет дверей, которые считаем проходимыми (для проверки от ключа к двери)
     * @return true если путь существует
     */
    private boolean isReachableWithoutDoors(
            Level level, Position from, Position to, KeyColor ignoreDoorColor) {
        if (from.equals(to)) {
            return true;
        }

        TileMap tileMap = level.getTileMap();
        Queue<Position> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();

        queue.add(from);
        visited.add(from);

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            for (int[] dir : dirs) {
                int nx = current.x() + dir[0];
                int ny = current.y() + dir[1];
                Position next = new Position(nx, ny);

                if (visited.contains(next)) continue;
                if (!tileMap.isWithinBounds(nx, ny)) continue;

                Tile tile = tileMap.getTile(nx, ny);
                if (tile == null) continue;

                // Проверяем проходимость
                if (!tile.isWalkable()) continue;

                // Проверка на дверь
                Door door = level.getDoorAt(nx, ny);
                if (door != null && door.isLocked()) {
                    // Если игнорируем двери этого цвета, считаем проходимой
                    if (ignoreDoorColor == null || door.getColor() != ignoreDoorColor) {
                        continue; // Запертая дверь непроходима
                    }
                }

                if (next.equals(to)) {
                    return true;
                }

                visited.add(next);
                queue.add(next);
            }
        }

        return false;
    }

    /** Проверяет, что все ключи на уровне достижимы. */
    public boolean areAllKeysReachable(Level level, Position startPosition) {
        Map<KeyColor, List<Position>> keyPositions = collectKeyPositions(level);

        for (List<Position> positions : keyPositions.values()) {
            for (Position keyPos : positions) {
                if (!isReachableWithoutDoors(level, startPosition, keyPos, null)) {
                    System.out.println("Ключ на позиции " + keyPos + " недостижим");
                    return false;
                }
            }
        }

        return true;
    }

    /** Проверяет, что все комнаты уровня достижимы. */
    public boolean areAllRoomsReachable(Level level, Position startPosition) {
        Set<Room> reachableRooms = new HashSet<>();

        Queue<Position> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();

        queue.add(startPosition);
        visited.add(startPosition);

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            // Добавляем комнату, если текущая позиция в комнате
            Room currentRoom = level.findRoomContaining(current.x(), current.y());
            if (currentRoom != null) {
                reachableRooms.add(currentRoom);
            }

            for (int[] dir : dirs) {
                int nx = current.x() + dir[0];
                int ny = current.y() + dir[1];
                Position next = new Position(nx, ny);

                if (visited.contains(next)) continue;
                if (!level.getTileMap().isWithinBounds(nx, ny)) continue;

                Tile tile = level.getTileMap().getTile(nx, ny);
                if (tile == null) continue;

                if (!tile.isWalkable()) continue;

                // Двери считаем проходимыми для проверки связности
                Door door = level.getDoorAt(nx, ny);
                if (door != null && door.isLocked()) {
                    // Запертые двери не учитываем для связности
                    continue;
                }

                visited.add(next);
                queue.add(next);
            }
        }

        // Проверяем, что все комнаты достижимы
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null && !reachableRooms.contains(room)) {
                    System.out.println(
                            "Validation failed: Room at (" + row + "," + col + ") is not reachable");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Полная валидация уровня с ограничением на количество проверок. Проверка на сфортлоки
     *
     * @param level уровень
     * @param startPosition стартовая позиция
     * @return true если уровень валиден
     */
    public boolean validateFull(Level level, Position startPosition) {
        // Защита от null
        if (level == null || startPosition == null) {
            System.out.println("Нет уровня");
            return false;
        }

        // Если нет дверей, считаем уровень валидным
        if (level.getDoors().isEmpty()) {
            return true;
        }

        // Проверка, что все комнаты достижимы (базовая связность)
        if (!areAllRoomsReachable(level, startPosition)) {
            return false;
        }

        // Проверка достижимости ключей
        if (!areAllKeysReachable(level, startPosition)) {
            return false;
        }

        // Основная проверка дверей и ключей
        if (!validate(level, startPosition)) {
            return false;
        }

        return true;
    }
}
