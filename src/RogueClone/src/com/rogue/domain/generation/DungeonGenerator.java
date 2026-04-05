package com.rogue.domain.generation;

import com.rogue.domain.*;
import com.rogue.domain.entities.*;
import com.rogue.domain.items.*;
import java.util.*;

public class DungeonGenerator {
    private static final int GRID_SIZE = 3;
    private static final int MIN_ROOM_SIZE = 5;
    private static final int MAX_ROOM_SIZE = 12;
    private static final int ROOM_SPACING = 2;

    private Random random;

    public DungeonGenerator() {
        this.random = new Random();
    }

    public Level generateLevel(int depth) {
        Level level = new Level(depth);
        Room[][] roomGrid = new Room[GRID_SIZE][GRID_SIZE];
        List<Room> rooms = new ArrayList<>();

        // Step 1: Generate rooms
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int width = MIN_ROOM_SIZE + random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
                int height = MIN_ROOM_SIZE + random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
                int x = j * (MAX_ROOM_SIZE + ROOM_SPACING) + 2 + random.nextInt(ROOM_SPACING);
                int y = i * (MAX_ROOM_SIZE + ROOM_SPACING) + 2 + random.nextInt(ROOM_SPACING);

                Room room = new Room(x, y, width, height);
                roomGrid[i][j] = room;
                rooms.add(room);
                level.addRoom(room);
            }
        }

        // Step 2: Connect rooms with corridors
        connectAdjacentRooms(level, roomGrid);

        // Step 3: Ensure connectivity
        ensureConnectivity(level, rooms);

        // Step 4: Set start and exit rooms
        Room startRoom = rooms.get(0);
        Room exitRoom = rooms.get(rooms.size() - 1);
        startRoom.setType(RoomType.START);
        exitRoom.setType(RoomType.EXIT);

        // Step 5: Populate with content based on depth
        populateLevel(level, depth);

        return level;
    }

    private void connectAdjacentRooms(Level level, Room[][] grid) {
        // Connect horizontally
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE - 1; j++) {
                Room roomA = grid[i][j];
                Room roomB = grid[i][j + 1];
                Corridor corridor = createCorridor(roomA, roomB);
                if (corridor != null) {
                    level.addCorridor(corridor);
                }
            }
        }

        // Connect vertically
        for (int i = 0; i < GRID_SIZE - 1; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Room roomA = grid[i][j];
                Room roomB = grid[i + 1][j];
                Corridor corridor = createCorridor(roomA, roomB);
                if (corridor != null) {
                    level.addCorridor(corridor);
                }
            }
        }
    }

    private Corridor createCorridor(Room roomA, Room roomB) {
        Position centerA = roomA.getCenter();
        Position centerB = roomB.getCenter();

        List<Position> corridorTiles = new ArrayList<>();
        int x = centerA.x;
        int y = centerA.y;

        // Horizontal movement
        while (x != centerB.x) {
            x += (centerB.x > x) ? 1 : -1;
            corridorTiles.add(new Position(x, y));
        }

        // Vertical movement
        while (y != centerB.y) {
            y += (centerB.y > y) ? 1 : -1;
            corridorTiles.add(new Position(x, y));
        }

        return new Corridor(roomA, roomB);
    }

    private void ensureConnectivity(Level level, List<Room> rooms) {
        Set<Room> connected = new HashSet<>();
        Queue<Room> queue = new LinkedList<>();
        queue.add(rooms.get(0));
        connected.add(rooms.get(0));

        while (!queue.isEmpty()) {
            Room current = queue.poll();
            for (Room neighbor : getNeighbors(current, level)) {
                if (!connected.contains(neighbor)) {
                    connected.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        for (Room room : rooms) {
            if (!connected.contains(room)) {
                Room nearest = findNearestRoom(room, connected);
                level.addCorridor(createCorridor(room, nearest));
                connected.add(room);
            }
        }
    }

    private List<Room> getNeighbors(Room room, Level level) {
        List<Room> neighbors = new ArrayList<>();
        for (Corridor corridor : level.getCorridors()) {
            if (corridor.getRoomA() != null && corridor.getRoomA().equals(room)) {
                neighbors.add(corridor.getRoomB());
            } else if (corridor.getRoomB() != null && corridor.getRoomB().equals(room)) {
                neighbors.add(corridor.getRoomA());
            }
        }
        return neighbors;
    }

    private Room findNearestRoom(Room room, Set<Room> connected) {
        Room nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Room other : connected) {
            double distance = room.getCenter().distanceTo(other.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = other;
            }
        }
        return nearest;
    }

    private void populateLevel(Level level, int depth) {
        Random random = new Random();

        for (Room room : level.getRooms()) {
            if (room.getType() == RoomType.START) continue;

            int enemyChance = Math.min(70, 30 + depth * 2);
            int itemChance = Math.max(20, 50 - depth);

            if (random.nextInt(100) < enemyChance) {
                int enemyCount = 1 + random.nextInt(1 + depth / 5);
                for (int i = 0; i < enemyCount; i++) {
                    Enemy enemy = createEnemyForDepth(depth);
                    Position pos = room.getRandomFloorPosition();
                    enemy.setPosition(pos);
                    level.addEnemy(enemy);
                }
            }

            if (random.nextInt(100) < itemChance) {
                int itemCount = random.nextInt(3);
                for (int i = 0; i < itemCount; i++) {
                    Item item = createRandomItem(depth);
                    Position pos = room.getRandomFloorPosition();
                    level.addItem(item, pos);
                }
            }
        }
    }

    private Enemy createEnemyForDepth(int depth) {
        EnemyType[] types = EnemyType.values();
        int index = Math.min(types.length - 1, depth / 5);
        if (index < 0) index = 0;

        if (Math.random() < 0.3 && index < types.length - 1) {
            index++;
        }

        return new Enemy(types[index]);
    }

    private Item createRandomItem(int depth) {
        Random random = new Random();
        int type = random.nextInt(100);

        if (type < 30) {
            return new Food("Ration", 10 + random.nextInt(20));
        } else if (type < 50) {
            String[] stats = {"strength", "dexterity"};
            String stat = stats[random.nextInt(stats.length)];
            return new Potion("Potion of " + stat, stat, 5 + random.nextInt(10), 15);
        } else if (type < 70) {
            String[] stats = {"health", "strength", "dexterity"};
            String stat = stats[random.nextInt(stats.length)];
            return new Scroll("Scroll of " + stat, stat, 3 + random.nextInt(7));
        } else if (type < 85) {
            String[] weapons = {"Dagger", "Sword", "Axe", "Mace"};
            String weaponName = weapons[random.nextInt(weapons.length)];
            return new Weapon(weaponName, 5 + random.nextInt(15), 5 + random.nextInt(10));
        } else {
            return new Treasure(20 + random.nextInt(80) + depth * 10);
        }
    }
}