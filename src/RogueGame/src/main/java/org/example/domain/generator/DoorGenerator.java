package org.example.domain.generator;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.KeyColor;
import org.example.domain.model.enums.TileType;

/**
 * Генератор дверей и ключей для уровня. Отвечает за размещение дверей на соединениях комнат и
 * генерацию соответствующих ключей.
 */
public class DoorGenerator {

    private final Random random = new Random();

    /**
     * Генерирует двери и ключи на уровне.
     *
     * @param level уровень
     * @param levelNumber номер уровня (влияет на количество и сложность)
     */
    public void generateDoorsAndKeys(Level level, int levelNumber) {
        // Собираем все точки соединения комнат с коридорами
        List<Position> connectionPoints = collectConnectionPoints(level);

        if (connectionPoints.isEmpty()) {
            System.out.println("Нет точек соединения");
            return;
        }

        // Определяем количество дверей (1-3 в зависимости от уровня)
        int doorCount = calculateDoorCount(levelNumber, connectionPoints.size());
        doorCount = Math.min(doorCount, connectionPoints.size());

        if (doorCount == 0) {
            return;
        }

        // Перемешиваем точки и выбираем случайные для дверей
        Collections.shuffle(connectionPoints, random);
        List<Position> doorPositions = connectionPoints.subList(0, doorCount);

        // Создаём двери со случайными цветами
        List<KeyColor> usedColors = createDoors(level, doorPositions, levelNumber);

        // Генерируем ключи для дверей
        generateKeysForDoors(level, usedColors, levelNumber);
    }

    /** Собирает все точки соединения комнат с коридорами. */
    private List<Position> collectConnectionPoints(Level level) {
        List<Position> points = new ArrayList<>();

        // Собираем все дверные проёмы из комнат
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null) {
                    points.addAll(room.getDoorways());
                }
            }
        }

        return points;
    }

    /** Рассчитывает количество дверей на уровне. */
    private int calculateDoorCount(int levelNumber, int maxDoors) {
        // На ранних уровнях мало дверей, на глубоких - больше
        if (levelNumber <= 3) {
            return Math.min(1, maxDoors);
        } else if (levelNumber <= 7) {
            return Math.min(2, maxDoors);
        } else if (levelNumber <= 12) {
            return Math.min(3, maxDoors);
        } else if (levelNumber <= 17) {
            return Math.min(4, maxDoors);
        } else {
            return Math.min(5, maxDoors);
        }
    }

    /**
     * Создаёт двери на указанных позициях.
     *
     * @return список использованных цветов дверей
     */
    private List<KeyColor> createDoors(Level level, List<Position> doorPositions, int levelNumber) {
        List<KeyColor> usedColors = new ArrayList<>();
        KeyColor[] colors = KeyColor.values();

        for (Position pos : doorPositions) {
            // Выбираем цвет для двери
            KeyColor color = selectDoorColor(levelNumber, colors);
            usedColors.add(color);

            Door door = new Door(pos.x(), pos.y(), color);
            level.addDoor(door);

            // Превращаем клетку в дверь
            level.getTileMap().setTileType(pos.x(), pos.y(), TileType.DOOR);
        }

        return usedColors;
    }

    /** Выбирает цвет для двери. */
    private KeyColor selectDoorColor(int levelNumber, KeyColor[] colors) {
        // На ранних уровнях используем только базовые цвета (красный, синий, жёлтый)
        if (levelNumber <= 5) {
            KeyColor[] basicColors = {KeyColor.RED, KeyColor.BLUE, KeyColor.YELLOW};
            return basicColors[random.nextInt(basicColors.length)];
        }

        // На средних уровнях добавляем зелёный
        if (levelNumber <= 10) {
            KeyColor[] mediumColors = {KeyColor.RED, KeyColor.BLUE, KeyColor.YELLOW, KeyColor.GREEN};
            return mediumColors[random.nextInt(mediumColors.length)];
        }

        // На глубоких уровнях - все цвета
        return colors[random.nextInt(colors.length)];
    }

    /** Генерирует ключи для дверей. */
    private void generateKeysForDoors(Level level, List<KeyColor> doorColors, int levelNumber) {
        if (doorColors.isEmpty()) {
            return;
        }

        // Получаем все проходимые позиции на уровне (кроме стартовой комнаты и дверей)
        List<Position> availablePositions = getAvailablePositionsForKeys(level);

        if (availablePositions.isEmpty()) {
            System.out.println("Нет доступных позиций для размещения ключей");
            return;
        }

        // Группируем цвета по количеству
        Map<KeyColor, Integer> colorCount = new HashMap<>();
        for (KeyColor color : doorColors) {
            colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
        }

        // Генерируем ключи для каждого цвета
        for (Map.Entry<KeyColor, Integer> entry : colorCount.entrySet()) {
            KeyColor color = entry.getKey();
            int neededKeys = entry.getValue();

            // Генерируем по одному ключу на каждую дверь
            for (int i = 0; i < neededKeys && !availablePositions.isEmpty(); i++) {
                int idx = random.nextInt(availablePositions.size());
                Position pos = availablePositions.get(idx);

                // Создаём ключ с правильными координатами
                Key key = new Key(color, pos.x(), pos.y());
                level.addItem(key, pos.x(), pos.y());

                System.out.println(
                        "Ключ создан по координатам (" + pos.x() + "," + pos.y() + ") цвет: " + color);

                availablePositions.remove(idx);
            }
        }
    }

    /** Получает позиции, доступные для размещения ключей. */
    private List<Position> getAvailablePositionsForKeys(Level level) {
        List<Position> positions = new ArrayList<>();

        Position startRoomPos = level.getStartRoomPosition();
        Room startRoom =
                startRoomPos != null ? level.getRoom(startRoomPos.x(), startRoomPos.y()) : null;

        for (Position pos : level.getTileMap().getAllWalkablePositions()) {
            // Не размещаем ключи в стартовой комнате
            if (startRoom != null && startRoom.contains(pos)) {
                continue;
            }
            // Не размещаем ключи на выходе
            if (level.isExit(pos.x(), pos.y())) {
                continue;
            }
            // Не размещаем ключи на дверях
            if (level.isDoor(pos.x(), pos.y())) {
                continue;
            }
            // Не размещаем ключи там, где уже есть предметы
            if (level.getItemAt(pos.x(), pos.y()) != null) {
                continue;
            }
            positions.add(pos);
        }

        System.out.println("Доступные позиции: " + positions.size());
        return positions;
    }
}
