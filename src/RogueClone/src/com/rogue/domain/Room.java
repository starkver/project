package com.rogue.domain;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private int x, y;
    private int width, height;
    private List<Position> floorTiles;
    private List<Position> wallTiles;
    private RoomType type;
    private boolean discovered;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.floorTiles = new ArrayList<>();
        this.wallTiles = new ArrayList<>();
        this.type = RoomType.NORMAL;
        this.discovered = false;
        generateTiles();
    }

    private void generateTiles() {
        for (int i = x + 1; i < x + width - 1; i++) {
            for (int j = y + 1; j < y + height - 1; j++) {
                floorTiles.add(new Position(i, j));
            }
        }

        for (int i = x; i < x + width; i++) {
            wallTiles.add(new Position(i, y));
            wallTiles.add(new Position(i, y + height - 1));
        }
        for (int j = y + 1; j < y + height - 1; j++) {
            wallTiles.add(new Position(x, j));
            wallTiles.add(new Position(x + width - 1, j));
        }
    }

    public boolean contains(Position pos) {
        return pos.x >= x && pos.x < x + width &&
                pos.y >= y && pos.y < y + height;
    }

    public Position getCenter() {
        return new Position(x + width / 2, y + height / 2);
    }

    public Position getRandomFloorPosition() {
        if (floorTiles.isEmpty()) return getCenter();
        int index = (int)(Math.random() * floorTiles.size());
        return floorTiles.get(index);
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Position> getFloorTiles() { return floorTiles; }
    public List<Position> getWallTiles() { return wallTiles; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public boolean isDiscovered() { return discovered; }
    public void setDiscovered(boolean discovered) { this.discovered = discovered; }
}