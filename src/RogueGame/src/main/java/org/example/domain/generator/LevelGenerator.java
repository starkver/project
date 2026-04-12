package org.example.domain.generator;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.MonsterType;
import org.example.domain.model.enums.TileType;

/**
 * Генератор уровней. Создаёт полный уровень с комнатами, коридорами, монстрами, предметами и
 * дверями.
 */
public class LevelGenerator {

    // room_count всегда = 9
    private static final int ROOMS_PER_ROW = 3;

    // Базовая конфигурация генерации (может изменяться коэффициентами сложности)
    private static final int BASE_MONSTER_COUNT = 3;
    private static final int MIN_ITEM_COUNT = 3;
    private static final int MAX_ITEM_COUNT = 15;

    // Коэффициенты сложности (устанавливаются из DifficultyBalanceService)
    private double difficultyMultiplier = 1.0;
    private double itemCountMultiplier = 1.0;
    private double monsterCountMultiplier = 1.0;
    private double usefulItemChanceMultiplier = 1.0;
    private double monsterDifficultyMultiplier = 1.0;
    private double elixirChanceMultiplier = 1.0;

    // Генератор случайных чисел
    private final Random random = new Random();

    // Генераторы дверей и валидатор
    private final DoorGenerator doorGenerator = new DoorGenerator();
    private final KeyDoorValidator keyDoorValidator = new KeyDoorValidator();

    /**
     * Генерирует уровень с учётом текущих коэффициентов сложности.
     *
     * @param levelNumber номер уровня (1-21)
     * @return сгенерированный уровень
     */
    public Level levelGenerate(int levelNumber) {
        Level level = new Level(levelNumber);

        // Генерация карты (комнаты и коридоры)
        mapGenerate(level);

        // Генерация выхода на следующий уровень
        levelTransitionGenerate(level);

        // Размещение игрока
        playerStartPositionGenerate(level);

        // Генерация дверей и ключей (используем DoorGenerator)
        doorGenerator.generateDoorsAndKeys(level, levelNumber);

        // Получаем стартовую позицию для валидации
        Position startPos = findStartPosition(level);

        // Генерация предметов с учётом сложности
        itemsGenerate(level, levelNumber);

        // Генерация монстров с учётом сложности
        monsterGenerate(level, levelNumber);

        // Валидация доступности ключей и дверей (софтлоки)
        if (!keyDoorValidator.validateFull(level, startPos)) {
            // Если валидация не пройдена, регенерируем уровень
            System.out.println("Валидация дверей не пройдена. Регенерация уровня " + levelNumber);
            return levelGenerate(levelNumber);
        }

        return level;
    }

    /**
     * Генерирует уровень с заданными коэффициентами сложности. Используется для балансировки
     * сложности.
     */
    public Level levelGenerate(
            int levelNumber,
            double difficultyMultiplier,
            double itemCountMultiplier,
            double monsterCountMultiplier,
            double usefulItemChanceMultiplier,
            double monsterDifficultyMultiplier,
            double elixirChanceMultiplier) {
        this.difficultyMultiplier = difficultyMultiplier;
        this.itemCountMultiplier = itemCountMultiplier;
        this.monsterCountMultiplier = monsterCountMultiplier;
        this.usefulItemChanceMultiplier = usefulItemChanceMultiplier;
        this.monsterDifficultyMultiplier = monsterDifficultyMultiplier;
        this.elixirChanceMultiplier = elixirChanceMultiplier;

        return levelGenerate(levelNumber);
    }

    private void mapGenerate(Level level) {
        TileMap tileMap = level.getTileMap();

        // Инициализация карты стенами
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                tileMap.setTileType(x, y, TileType.WALL);
            }
        }

        // Генерация комнат
        List<Room> rooms = generateRooms();
        for (int i = 0; i < rooms.size(); i++) {
            int row = i / ROOMS_PER_ROW;
            int col = i % ROOMS_PER_ROW;
            level.setRoom(row, col, rooms.get(i));
        }

        // Генерация коридоров (простые прямые)
        List<Corridor> corridors = connectRooms(rooms);
        for (Corridor corridor : corridors) {
            level.addCorridor(corridor);
        }

        // Заполнение полов в комнатах
        for (Room room : rooms) {
            for (Position pos : room.getAllInteriorPositions()) {
                tileMap.setTileType(pos.x(), pos.y(), TileType.FLOOR);
            }
        }

        // Заполнение коридоров
        for (Corridor corridor : corridors) {
            for (Position pos : corridor.getTiles()) {
                tileMap.setTileType(pos.x(), pos.y(), TileType.CORRIDOR);
            }
        }
    }

    // Создается сетка 3 на 3 и соединяется коридорами. Такая сетка всегда связана, поэтому нет
    // необходимости в
    // проверке связности графа
    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();

        int mapWidth = 90;
        int mapHeight = 30;

        int regionWidth = mapWidth / 3;
        int regionHeight = mapHeight / 3;
        int minRoomSize = 5;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int left = col * regionWidth;
                int right = (col + 1) * regionWidth;
                int top = row * regionHeight;
                int bottom = (row + 1) * regionHeight;

                int maxWidth = right - left - 2;
                int maxHeight = bottom - top - 2;

                int roomWidth = minRoomSize + random.nextInt(Math.max(1, maxWidth - minRoomSize + 1));
                int roomHeight = minRoomSize + random.nextInt(Math.max(1, maxHeight - minRoomSize + 1));

                int offsetX = 1 + random.nextInt(Math.max(1, maxWidth - roomWidth));
                int offsetY = 1 + random.nextInt(Math.max(1, maxHeight - roomHeight));

                Position topLeft = new Position(left + offsetX, top + offsetY);
                Room room = new Room(topLeft, roomWidth, roomHeight);

                rooms.add(room);
            }
        }

        return rooms;
    }

    /** Соединяет комнаты прямыми коридорами (только соседние по сетке) */
    private List<Corridor> connectRooms(List<Room> rooms) {
        List<Corridor> corridors = new ArrayList<>();

        // Соединяем соседние комнаты по горизонтали
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int index = row * 3 + col;
                Room leftRoom = rooms.get(index);
                Room rightRoom = rooms.get(index + 1);

                Corridor corridor = createHorizontalCorridor(leftRoom, rightRoom);
                if (corridor != null) {
                    corridors.add(corridor);
                }
            }
        }

        // Соединяем соседние комнаты по вертикали
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int topIndex = row * 3 + col;
                int bottomIndex = (row + 1) * 3 + col;
                Room topRoom = rooms.get(topIndex);
                Room bottomRoom = rooms.get(bottomIndex);

                Corridor corridor = createVerticalCorridor(topRoom, bottomRoom);
                if (corridor != null) {
                    corridors.add(corridor);
                }
            }
        }

        return corridors;
    }

    /** Создает горизонтальный коридор между левой и правой комнатами */
    private Corridor createHorizontalCorridor(Room leftRoom, Room rightRoom) {
        // Находим y координату для коридора (посередине между комнатами)
        int leftCenterY = leftRoom.getTopLeft().y() + leftRoom.getHeight() / 2;
        int rightCenterY = rightRoom.getTopLeft().y() + rightRoom.getHeight() / 2;
        int corridorY = (leftCenterY + rightCenterY) / 2;

        // Проверяем, что коридор проходит через обе комнаты
        boolean inLeftRoom =
                corridorY >= leftRoom.getTopLeft().y() + 1
                        && corridorY <= leftRoom.getTopLeft().y() + leftRoom.getHeight() - 2;
        boolean inRightRoom =
                corridorY >= rightRoom.getTopLeft().y() + 1
                        && corridorY <= rightRoom.getTopLeft().y() + rightRoom.getHeight() - 2;

        if (!inLeftRoom || !inRightRoom) {
            // Пробуем найти альтернативную Y
            int bestY = -1;
            int minDiff = Integer.MAX_VALUE;

            for (int y = Math.max(leftRoom.getTopLeft().y() + 1, rightRoom.getTopLeft().y() + 1);
                 y
                         <= Math.min(
                         leftRoom.getTopLeft().y() + leftRoom.getHeight() - 2,
                         rightRoom.getTopLeft().y() + rightRoom.getHeight() - 2);
                 y++) {
                int diff = Math.abs(y - leftCenterY) + Math.abs(y - rightCenterY);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestY = y;
                }
            }

            if (bestY == -1) {
                // Нет подходящей Y, не можем создать коридор
                return null;
            }
            corridorY = bestY;
        }

        // Находим точки на стенах
        int leftX = leftRoom.getTopLeft().x() + leftRoom.getWidth() - 1; // правая стена левой комнаты
        int rightX = rightRoom.getTopLeft().x(); // левая стена правой комнаты

        // Проверяем, что между комнатами есть пространство
        if (leftX + 1 > rightX) {
            // Комнаты уже касаются друг друга
            return null;
        }

        Position leftPoint = new Position(leftX, corridorY);
        Position rightPoint = new Position(rightX, corridorY);

        // Генерируем коридор (включая стены комнат)
        List<Position> tiles = new ArrayList<>();

        // Добавляем клетки от левой комнаты до правой (включая точки входа)
        for (int x = leftX; x <= rightX; x++) {
            tiles.add(new Position(x, corridorY));
        }

        // Добавляем дверные проёмы
        leftRoom.addDoorway(leftPoint);
        rightRoom.addDoorway(rightPoint);

        return new Corridor(leftRoom, rightRoom, leftPoint, rightPoint, tiles);
    }

    /** Создает вертикальный коридор между верхней и нижней комнатами */
    private Corridor createVerticalCorridor(Room topRoom, Room bottomRoom) {
        // Находим x координату для коридора (посередине между комнатами)
        int topCenterX = topRoom.getTopLeft().x() + topRoom.getWidth() / 2;
        int bottomCenterX = bottomRoom.getTopLeft().x() + bottomRoom.getWidth() / 2;
        int corridorX = (topCenterX + bottomCenterX) / 2;

        // Проверяем, что коридор проходит через обе комнаты
        boolean inTopRoom =
                corridorX >= topRoom.getTopLeft().x() + 1
                        && corridorX <= topRoom.getTopLeft().x() + topRoom.getWidth() - 2;
        boolean inBottomRoom =
                corridorX >= bottomRoom.getTopLeft().x() + 1
                        && corridorX <= bottomRoom.getTopLeft().x() + bottomRoom.getWidth() - 2;

        if (!inTopRoom || !inBottomRoom) {
            // Пробуем найти альтернативную X
            int bestX = -1;
            int minDiff = Integer.MAX_VALUE;

            for (int x = Math.max(topRoom.getTopLeft().x() + 1, bottomRoom.getTopLeft().x() + 1);
                 x
                         <= Math.min(
                         topRoom.getTopLeft().x() + topRoom.getWidth() - 2,
                         bottomRoom.getTopLeft().x() + bottomRoom.getWidth() - 2);
                 x++) {
                int diff = Math.abs(x - topCenterX) + Math.abs(x - bottomCenterX);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestX = x;
                }
            }

            if (bestX == -1) {
                // Нет подходящей X, не можем создать коридор
                return null;
            }
            corridorX = bestX;
        }

        // Находим точки на стенах
        int topY = topRoom.getTopLeft().y() + topRoom.getHeight() - 1; // нижняя стена верхней комнаты
        int bottomY = bottomRoom.getTopLeft().y(); // верхняя стена нижней комнаты

        // Проверяем, что между комнатами есть пространство
        if (topY + 1 > bottomY) {
            // Комнаты уже касаются друг друга
            return null;
        }

        Position topPoint = new Position(corridorX, topY);
        Position bottomPoint = new Position(corridorX, bottomY);

        // Генерируем коридор (включая стены комнат)
        List<Position> tiles = new ArrayList<>();

        // Добавляем клетки от верхней комнаты до нижней (включая точки входа)
        for (int y = topY; y <= bottomY; y++) {
            tiles.add(new Position(corridorX, y));
        }

        // Добавляем дверные проёмы
        topRoom.addDoorway(topPoint);
        bottomRoom.addDoorway(bottomPoint);

        return new Corridor(topRoom, bottomRoom, topPoint, bottomPoint, tiles);
    }

    private void levelTransitionGenerate(Level level) {
        Position startPos = level.getStartRoomPosition();
        Room startRoom = startPos != null ? level.getRoom(startPos.x(), startPos.y()) : null;

        int exitRow, exitCol;
        do {
            exitRow = random.nextInt(3);
            exitCol = random.nextInt(3);
        } while (startPos != null && exitRow == startPos.x() && exitCol == startPos.y());

        Room exitRoom = level.getRoom(exitRow, exitCol);
        exitRoom.setExitRoom(true);
        level.setExitRoomPosition(new Position(exitRow, exitCol));

        Position exitTilePos = exitRoom.getRandomInteriorPosition(random);
        level.setExitTilePosition(exitTilePos);
        level.getTileMap().setTileType(exitTilePos.x(), exitTilePos.y(), TileType.EXIT);
    }

    private void itemsGenerate(Level level, int levelNumber) {
        TileMap tileMap = level.getTileMap();
        List<Position> walkablePositions = tileMap.getAllWalkablePositions();

        // Исключаем позиции с дверями и выходом
        walkablePositions.removeIf(
                pos -> level.isDoor(pos.x(), pos.y()) || level.isExit(pos.x(), pos.y()));

        // Исключаем стартовую комнату
        Position startRoomPos = level.getStartRoomPosition();
        Room startRoom =
                startRoomPos != null ? level.getRoom(startRoomPos.x(), startRoomPos.y()) : null;
        if (startRoom != null) {
            walkablePositions.removeIf(pos -> startRoom.contains(pos));
        }

        // Расчёт количества предметов с учётом сложности
        int baseItemCount = Math.max(MIN_ITEM_COUNT, MAX_ITEM_COUNT - levelNumber / 2);
        int itemCount =
                (int)
                        Math.max(MIN_ITEM_COUNT, Math.min(MAX_ITEM_COUNT, baseItemCount * itemCountMultiplier));

        itemCount = Math.min(itemCount, walkablePositions.size());

        for (int i = 0; i < itemCount && !walkablePositions.isEmpty(); i++) {
            int idx = random.nextInt(walkablePositions.size());
            Position pos = walkablePositions.get(idx);

            Item item = createRandomItem(levelNumber, random);

            if (item != null) {
                level.addItem(item, pos.x(), pos.y());
            }

            walkablePositions.remove(idx);
        }
    }

    private Item createRandomItem(int levelNumber, Random random) {
        // Базовая вероятность для каждого типа предмета
        double[] probabilities = calculateItemProbabilities(levelNumber);
        double roll = random.nextDouble();

        double cumulative = 0.0;

        // Еда
        cumulative += probabilities[0];
        if (roll < cumulative) {
            return createRandomFood(random);
        }

        // Эликсиры
        cumulative += probabilities[1];
        if (roll < cumulative) {
            return createRandomElixir(random);
        }

        // Свитки
        cumulative += probabilities[2];
        if (roll < cumulative) {
            return createRandomScroll(random);
        }

        // Оружие
        cumulative += probabilities[3];
        if (roll < cumulative) {
            return createRandomWeapon(random);
        }

        // Сокровища (оставшаяся вероятность)
        return createRandomTreasure(random);
    }

    private double[] calculateItemProbabilities(int levelNumber) {
        // Базовые вероятности (сумма = 1.0)
        double foodProb = 0.25;
        double elixirProb = 0.20;
        double scrollProb = 0.15;
        double weaponProb = 0.15;
        double treasureProb = 0.25;

        // Корректировка с учётом номера уровня
        double levelFactor = Math.max(0.5, 1.0 - (levelNumber - 1) * 0.02);

        // Корректировка с учётом сложности
        double difficultyFactor = usefulItemChanceMultiplier;

        double usefulMultiplier = levelFactor * difficultyFactor;

        foodProb = foodProb * usefulMultiplier;
        elixirProb = elixirProb * usefulMultiplier;
        scrollProb = scrollProb * usefulMultiplier;
        treasureProb = treasureProb * (1.0 / Math.max(0.5, usefulMultiplier));

        double total = foodProb + elixirProb + scrollProb + weaponProb + treasureProb;

        foodProb /= total;
        elixirProb /= total;
        scrollProb /= total;
        weaponProb /= total;
        treasureProb /= total;

        return new double[] {foodProb, elixirProb, scrollProb, weaponProb, treasureProb};
    }

    private Food createRandomFood(Random random) {
        ItemSubType[] foods = {ItemSubType.BREAD, ItemSubType.MEAT, ItemSubType.APPLE};

        if (random.nextDouble() < difficultyMultiplier * 0.3) {
            return new Food(ItemSubType.MEAT);
        }

        return new Food(foods[random.nextInt(foods.length)]);
    }

    private Elixir createRandomElixir(Random random) {
        ItemSubType[] elixirs = {
                ItemSubType.ELIXIR_STRENGTH, ItemSubType.ELIXIR_AGILITY, ItemSubType.ELIXIR_MAX_HEALTH
        };

        if (random.nextDouble() < elixirChanceMultiplier * 0.4) {
            return new Elixir(ItemSubType.ELIXIR_MAX_HEALTH);
        }

        return new Elixir(elixirs[random.nextInt(elixirs.length)]);
    }

    private Scroll createRandomScroll(Random random) {
        ItemSubType[] scrolls = {
                ItemSubType.SCROLL_STRENGTH, ItemSubType.SCROLL_AGILITY, ItemSubType.SCROLL_MAX_HEALTH
        };
        return new Scroll(scrolls[random.nextInt(scrolls.length)]);
    }

    private Weapon createRandomWeapon(Random random) {
        ItemSubType[] weapons = {ItemSubType.SHORT_SWORD, ItemSubType.LONG_SWORD, ItemSubType.AXE};

        if (random.nextDouble() < difficultyMultiplier * 0.2) {
            return new Weapon(ItemSubType.LONG_SWORD);
        }

        return new Weapon(weapons[random.nextInt(weapons.length)]);
    }

    private Treasure createRandomTreasure(Random random) {
        ItemSubType[] treasures = {
                ItemSubType.SILVER_COIN,
                ItemSubType.GOLD_COIN,
                ItemSubType.SILVER_BAR,
                ItemSubType.GOLD_BAR,
                ItemSubType.GEM
        };

        if (random.nextDouble() < difficultyMultiplier * 0.3) {
            return new Treasure(ItemSubType.GEM);
        }

        return new Treasure(treasures[random.nextInt(treasures.length)]);
    }

    private void monsterGenerate(Level level, int levelNumber) {
        TileMap tileMap = level.getTileMap();
        List<Position> possiblePositions = new ArrayList<>();

        Position startRoomPos = level.getStartRoomPosition();
        Room startRoom =
                startRoomPos != null ? level.getRoom(startRoomPos.x(), startRoomPos.y()) : null;

        for (Position pos : tileMap.getAllWalkablePositions()) {
            if (startRoom != null && startRoom.contains(pos)) continue;
            if (level.isExit(pos.x(), pos.y())) continue;
            if (level.isDoor(pos.x(), pos.y())) continue;
            if (tileMap.hasMonster(pos.x(), pos.y())) continue;
            possiblePositions.add(pos);
        }

        int baseMonsterCount = BASE_MONSTER_COUNT + levelNumber / 2;
        int monsterCount =
                (int)
                        Math.max(
                                1, Math.min(possiblePositions.size(), baseMonsterCount * monsterCountMultiplier));

        monsterCount = Math.min(monsterCount + levelNumber / 3, possiblePositions.size());

        for (int i = 0; i < monsterCount && !possiblePositions.isEmpty(); i++) {
            int idx = random.nextInt(possiblePositions.size());
            Position pos = possiblePositions.get(idx);

            Monster monster = createRandomMonster(levelNumber, random, pos);

            if (monster != null) {
                level.addMonster(monster);
            }

            possiblePositions.remove(idx);
        }
    }

    private Monster createRandomMonster(int levelNumber, Random random, Position pos) {
        MonsterType[] types = MonsterType.values();

        int baseMaxTypeIndex = Math.min(types.length - 1, levelNumber / 3);

        int adjustedMaxIndex;
        if (monsterDifficultyMultiplier < 0.8) {
            adjustedMaxIndex = Math.max(0, baseMaxTypeIndex - 2);
        } else if (monsterDifficultyMultiplier > 1.2) {
            adjustedMaxIndex = Math.min(types.length - 1, baseMaxTypeIndex + 1);
        } else {
            adjustedMaxIndex = baseMaxTypeIndex;
        }

        adjustedMaxIndex = Math.max(0, Math.min(types.length - 1, adjustedMaxIndex));

        int typeIndex = random.nextInt(adjustedMaxIndex + 1);
        MonsterType type = types[typeIndex];

        Monster monster = new Monster(pos.x(), pos.y(), type);

        if (monsterDifficultyMultiplier != 1.0) {
            adjustMonsterStats(monster, monsterDifficultyMultiplier);
        }

        return monster;
    }

    private void adjustMonsterStats(Monster monster, double multiplier) {
        if (multiplier == 1.0) return;

        int newHealth = (int) (monster.getMaxHealth() * multiplier);
        int newStrength = (int) (monster.getStrength() * multiplier);
        int newAgility = (int) (monster.getAgility() * multiplier);

        monster.setMaxHealth(Math.max(1, newHealth));
        monster.setHealth(Math.min(monster.getHealth(), monster.getMaxHealth()));
        monster.setStrength(Math.max(1, newStrength));
        monster.setAgility(Math.max(1, newAgility));
    }

    private void playerStartPositionGenerate(Level level) {
        TileMap tileMap = level.getTileMap();

        Position exitRoomPos = level.getExitRoomPosition();
        Room exitRoom = exitRoomPos != null ? level.getRoom(exitRoomPos.x(), exitRoomPos.y()) : null;

        List<Room> startCandidates = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null && room != exitRoom) {
                    startCandidates.add(room);
                }
            }
        }

        if (startCandidates.isEmpty()) {
            startCandidates.add(level.getRoom(0, 0));
        }

        Room startRoom = startCandidates.get(random.nextInt(startCandidates.size()));
        startRoom.setStartRoom(true);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (level.getRoom(row, col) == startRoom) {
                    level.setStartRoomPosition(new Position(row, col));
                    break;
                }
            }
        }

        Position startPos = startRoom.getRandomInteriorPosition(random);
        tileMap.setPlayerPosition(startPos.x(), startPos.y());
    }

    /** Находит безопасную стартовую позицию (не на выходе и не на двери) */
    public Position findSafeStartPosition(Level level) {
        Position startRoomGridPos = level.getStartRoomPosition();

        if (startRoomGridPos == null) {
            // Если стартовая комната не найдена, ищем любую комнату
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    Room room = level.getRoom(row, col);
                    if (room != null) {
                        startRoomGridPos = new Position(row, col);
                        break;
                    }
                }
            }
        }

        if (startRoomGridPos != null) {
            Room startRoom = level.getRoom(startRoomGridPos.x(), startRoomGridPos.y());
            if (startRoom != null) {
                Position exitPos = level.getExitTilePosition();
                List<Position> safePositions = new ArrayList<>();

                for (Position pos : startRoom.getAllInteriorPositions()) {
                    // Исключаем выход
                    if (exitPos != null && pos.x() == exitPos.x() && pos.y() == exitPos.y()) {
                        continue;
                    }
                    // Исключаем двери
                    if (level.isDoor(pos.x(), pos.y())) {
                        continue;
                    }
                    safePositions.add(pos);
                }

                if (!safePositions.isEmpty()) {
                    Random rand = new Random();
                    return safePositions.get(rand.nextInt(safePositions.size()));
                }

                // Если нет безопасных позиций, возвращаем центр комнаты
                return startRoom.getCenter();
            }
        }

        // Запасной вариант - центр карты
        TileMap tileMap = level.getTileMap();
        return new Position(tileMap.getWidth() / 2, tileMap.getHeight() / 2);
    }

    public Position findStartPosition(Level level) {
        Position startRoomPos = level.getStartRoomPosition();

        if (startRoomPos != null) {
            Room startRoom = level.getRoom(startRoomPos.x(), startRoomPos.y());
            if (startRoom != null) {
                List<Position> interiorPositions = startRoom.getAllInteriorPositions();
                if (!interiorPositions.isEmpty()) {
                    return interiorPositions.get(random.nextInt(interiorPositions.size()));
                }
                return startRoom.getCenter();
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null) {
                    List<Position> interiorPositions = room.getAllInteriorPositions();
                    if (!interiorPositions.isEmpty()) {
                        return interiorPositions.get(random.nextInt(interiorPositions.size()));
                    }
                    return room.getCenter();
                }
            }
        }

        TileMap tileMap = level.getTileMap();
        return new Position(tileMap.getWidth() / 2, tileMap.getHeight() / 2);
    }
}
