package com.rogue.domain;

import com.rogue.domain.entities.Enemy;
import com.rogue.domain.entities.Player;
import com.rogue.domain.items.Item;
import java.util.*;

public class Level {
    private int depth;
    private List<Room> rooms;
    private List<Corridor> corridors;
    private Map<Position, Enemy> enemies;
    private Map<Position, Item> items;
    private Map<Position, Boolean> walls;
    private Set<Position> walkableTiles;
    private Position exitPosition;
    private boolean[][] explored;

    public Level(int depth) {
        this.depth = depth;
        this.rooms = new ArrayList<>();
        this.corridors = new ArrayList<>();
        this.enemies = new HashMap<>();
        this.items = new HashMap<>();
        this.walls = new HashMap<>();
        this.walkableTiles = new HashSet<>();
    }

    public void addRoom(Room room) {
        rooms.add(room);
        // Add floor tiles to walkable set
        for (Position pos : room.getFloorTiles()) {
            walkableTiles.add(pos);
        }
        // Add walls to wall map
        for (Position pos : room.getWallTiles()) {
            walls.put(pos, true);
        }
    }

    public void addCorridor(Corridor corridor) {
        corridors.add(corridor);
        for (Position pos : corridor.getTiles()) {
            walkableTiles.add(pos);
        }
    }

    public boolean isWalkable(Position pos) {
        return walkableTiles.contains(pos) && !enemies.containsKey(pos);
    }

    public boolean isWall(Position pos) {
        return walls.containsKey(pos);
    }

    public void addEnemy(Enemy enemy) {
        enemies.put(enemy.getPosition(), enemy);
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy.getPosition());
    }

    public Enemy getEnemyAt(Position pos) {
        return enemies.get(pos);
    }

    public void addItem(Item item, Position pos) {
        items.put(pos, item);
    }

    public Item getItemAt(Position pos) {
        return items.get(pos);
    }

    public Item removeItem(Position pos) {
        return items.remove(pos);
    }

    public Room getRoomAt(Position pos) {
        for (Room room : rooms) {
            if (room.contains(pos)) {
                return room;
            }
        }
        return null;
    }

    public Room getStartRoom() {
        for (Room room : rooms) {
            if (room.getType() == RoomType.START) {
                return room;
            }
        }
        return rooms.get(0);
    }

    public Room getExitRoom() {
        for (Room room : rooms) {
            if (room.getType() == RoomType.EXIT) {
                return room;
            }
        }
        return rooms.get(rooms.size() - 1);
    }

    public void setExitPosition(Position pos) {
        this.exitPosition = pos;
    }

    public boolean isExit(Position pos) {
        return exitPosition != null && exitPosition.equals(pos);
    }

    public void exploreArea(Position center, int radius, Player player) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                Position pos = new Position(center.x + dx, center.y + dy);
                Room room = getRoomAt(pos);
                if (room != null) {
                    room.setDiscovered(true);
                }
            }
        }
    }

    // Getters
    public int getDepth() { return depth; }
    public List<Room> getRooms() { return rooms; }
    public List<Corridor> getCorridors() { return corridors; }
    public Map<Position, Enemy> getEnemies() { return enemies; }
    public Map<Position, Item> getItems() { return items; }
    public Set<Position> getWalkableTiles() { return walkableTiles; }
}