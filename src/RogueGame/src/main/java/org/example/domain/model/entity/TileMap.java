package org.example.domain.model.entity;

import java.util.*;
import org.example.domain.model.enums.TileType;

/**
 * Класс, представляющий карту уровня (сетку клеток). Хранит все тайлы и обеспечивает доступ к ним
 * по координатам.
 */
public class TileMap {
    private final Tile[][] tiles;
    private final int width;
    private final int height;

    // Позиция игрока на карте (для быстрого доступа)
    private Position playerPosition;

    public TileMap() {
        this.width = 90;
        this.height = 30;
        this.tiles = new Tile[height][width];

        // Заполняем пустыми клетками (по умолчанию - стены)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(TileType.WALL);
            }
        }

        this.playerPosition = null;
    }

    /** Получить клетку по координатам. */
    public Tile getTile(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        return tiles[y][x];
    }

    public TileType getTileType(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null ? tile.getTileType() : null;
    }

    /** Получить клетку по позиции. */
    public Tile getTile(Position pos) {
        return getTile(pos.x(), pos.y());
    }

    /** Установить тип клетки по координатам. */
    public void setTileType(int x, int y, TileType type) {
        if (isWithinBounds(x, y)) {
            getTile(x, y).setTileType(type);
        }
    }

    /** Проверить, находятся ли координаты в пределах карты. */
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Проверить, находится ли позиция в пределах карты. */
    public boolean isWithinBounds(Position pos) {
        return isWithinBounds(pos.x(), pos.y());
    }

    /** Установить позицию игрока. Обновляет положение игрока на карте. */
    public void setPlayerPosition(int x, int y) {
        if (isWithinBounds(x, y)) {
            this.playerPosition = new Position(x, y);
        }
    }

    /** Проверить, находится ли игрок в указанной клетке. */
    public boolean isPlayerAt(int x, int y) {
        return playerPosition != null && playerPosition.x() == x && playerPosition.y() == y;
    }

    /** Проверить, находится ли игрок в указанной клетке. */
    public boolean isPlayerAt(Position pos) {
        return isPlayerAt(pos.x(), pos.y());
    }

    /** Получить монстра из клетки. */
    public Monster getMonster(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null ? tile.getMonster() : null;
    }

    /** Установить монстра в клетку. Автоматически обновляет координаты монстра. */
    public void setMonster(int x, int y, Monster monster) {
        Tile tile = getTile(x, y);
        if (tile != null) {
            tile.setMonster(monster);
            if (monster != null) {
                monster.setX(x);
                monster.setY(y);
            }
        }
    }

    /** Удалить монстра из клетки. */
    public void removeMonster(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile != null && tile.hasMonster()) {
            tile.removeMonster();
        }
    }

    /** Проверить, есть ли монстр в клетке. */
    public boolean hasMonster(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null && tile.hasMonster();
    }

    /** Получить предмет из клетки. */
    public Item getItem(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null ? tile.getItem() : null;
    }

    /** Установить предмет в клетку. */
    public void setItem(int x, int y, Item item) {
        Tile tile = getTile(x, y);
        if (tile != null) {
            tile.setItem(item);
        }
    }

    /** Получить все проходимые клетки. */
    public List<Position> getAllWalkablePositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].isWalkable()) {
                    positions.add(new Position(x, y));
                }
            }
        }
        return positions;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
