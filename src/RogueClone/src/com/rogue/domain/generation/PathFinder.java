package com.rogue.domain.generation;

import com.rogue.domain.*;
import java.util.*;

public class PathFinder {

    public static List<Position> findPath(Position start, Position end, Level level) {
        Map<Position, Position> cameFrom = new HashMap<>();
        Map<Position, Integer> costSoFar = new HashMap<>();
        PriorityQueue<Position> frontier = new PriorityQueue<>((a, b) ->
                Integer.compare(costSoFar.get(a) + heuristic(a, end),
                        costSoFar.get(b) + heuristic(b, end)));

        frontier.add(start);
        cameFrom.put(start, null);
        costSoFar.put(start, 0);

        while (!frontier.isEmpty()) {
            Position current = frontier.poll();

            if (current.equals(end)) {
                break;
            }

            for (Direction dir : Direction.values()) {
                Position next = current.add(dir);

                if (!isWalkable(next, level)) {
                    continue;
                }

                int newCost = costSoFar.get(current) + 1;

                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                    costSoFar.put(next, newCost);
                    int priority = newCost + heuristic(next, end);
                    frontier.add(next);
                    cameFrom.put(next, current);
                }
            }
        }

        return reconstructPath(cameFrom, start, end);
    }

    private static int heuristic(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static boolean isWalkable(Position pos, Level level) {
        if (level.isWall(pos)) return false;
        if (level.getEnemyAt(pos) != null) return false;
        return true;
    }

    private static List<Position> reconstructPath(Map<Position, Position> cameFrom,
                                                  Position start, Position end) {
        List<Position> path = new ArrayList<>();
        Position current = end;

        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        return path;
    }

    public static Position getRandomWalkablePosition(Level level, Room room) {
        List<Position> walkable = new ArrayList<>();
        for (Position pos : room.getFloorTiles()) {
            if (level.getEnemyAt(pos) == null && level.getItemAt(pos) == null) {
                walkable.add(pos);
            }
        }

        if (walkable.isEmpty()) {
            return room.getCenter();
        }

        return walkable.get(new Random().nextInt(walkable.size()));
    }
}