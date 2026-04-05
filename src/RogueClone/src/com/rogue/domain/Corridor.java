package com.rogue.domain;

import java.util.ArrayList;
import java.util.List;

public class Corridor {
    private List<Position> tiles;
    private Room roomA;
    private Room roomB;

    public Corridor(Room roomA, Room roomB) {
        this.roomA = roomA;
        this.roomB = roomB;
        this.tiles = new ArrayList<>();
        generateCorridor();
    }

    private void generateCorridor() {
        Position centerA = roomA.getCenter();
        Position centerB = roomB.getCenter();

        int currentX = centerA.x;
        int currentY = centerA.y;

        while (currentX != centerB.x) {
            currentX += (centerB.x > currentX) ? 1 : -1;
            tiles.add(new Position(currentX, currentY));
        }

        while (currentY != centerB.y) {
            currentY += (centerB.y > currentY) ? 1 : -1;
            tiles.add(new Position(currentX, currentY));
        }
    }

    public boolean contains(Position pos) {
        return tiles.contains(pos);
    }

    public List<Position> getTiles() { return tiles; }
    public Room getRoomA() { return roomA; }
    public Room getRoomB() { return roomB; }
}