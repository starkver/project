package org.example.domain.model.entity;

import org.example.domain.model.enums.TileType;

/**
 * Класс, представляющий одну клетку игрового мира. Хранит тип клетки, содержимое (монстр, предмет)
 * и состояние видимости.
 */
public class Tile {
    private TileType tileType;
    private Item item;
    private Monster monster;

    // Состояния для тумана войны
    private boolean isVisible; // видима ли клетка сейчас
    private boolean wasVisited; // была ли клетка когда-либо посещена (увидена)

    public Tile(TileType tileType) {
        this.tileType = tileType;
        this.item = null;
        this.monster = null;
        this.isVisible = false;
        this.wasVisited = false;
    }

    /** Проверить, можно ли пройти через эту клетку. */
    public boolean isWalkable() {
        return tileType.isWalkable();
    }

    /** Проверить, является ли клетка стеной. */
    public boolean isWall() {
        return tileType == TileType.WALL;
    }

    /** Проверить, является ли клетка полом комнаты. */
    public boolean isFloor() {
        return tileType == TileType.FLOOR;
    }

    /** Проверить, является ли клетка коридором. */
    public boolean isCorridor() {
        return tileType == TileType.CORRIDOR;
    }

    /** Проверить, является ли клетка выходом. */
    public boolean isExit() {
        return tileType == TileType.EXIT;
    }

    /** Проверить, есть ли на клетке живой монстр. */
    public boolean hasMonster() {
        return monster != null && monster.isAlive();
    }

    /** Получить монстра с клетки. */
    public Monster getMonster() {
        return monster;
    }

    /** Установить монстра на клетку. Автоматически обновляет координаты монстра. */
    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    /** Удалить монстра с клетки (например, после смерти). */
    public void removeMonster() {
        this.monster = null;
    }

    /** Проверить, есть ли на клетке предмет. */
    public boolean hasItem() {
        return item != null;
    }

    /** Получить предмет с клетки. */
    public Item getItem() {
        return item;
    }

    /** Установить предмет на клетку. */
    public void setItem(Item item) {
        this.item = item;
    }

    /** Проверить, видима ли клетка сейчас. */
    public boolean isVisible() {
        return isVisible;
    }

    /** Установить видимость клетки. */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if (visible) {
            this.wasVisited = true; // Если клетка видима, она автоматически посещена
        }
    }

    /** Проверить, была ли клетка когда-либо посещена. */
    public boolean wasVisited() {
        return wasVisited;
    }

    /** Установить флаг посещения. */
    public void setWasVisited(boolean visited) {
        this.wasVisited = visited;
    }

    /** Получить тип клетки. */
    public TileType getTileType() {
        return tileType;
    }

    /** Установить тип клетки. */
    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tile{type=").append(tileType);
        if (hasMonster()) sb.append(", monster=").append(monster.getMonsterType());
        if (hasItem()) sb.append(", item=").append(item.getName());
        sb.append(", visible=").append(isVisible);
        sb.append(", visited=").append(wasVisited);
        sb.append('}');
        return sb.toString();
    }
}
