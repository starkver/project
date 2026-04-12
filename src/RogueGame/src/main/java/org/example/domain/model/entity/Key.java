package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemType;
import org.example.domain.model.enums.KeyColor;

public class Key extends Item {
    private final KeyColor color;
    private int x;
    private int y;

    public Key(KeyColor color) {
        super(color.getDisplayName() + " ключ", ItemType.KEY, color.toItemSubType(), 1);
        this.color = color;
        this.x = 0;
        this.y = 0;
    }

    public Key(KeyColor color, int x, int y) {
        super(color.getDisplayName() + " ключ", ItemType.KEY, color.toItemSubType(), 1);
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public KeyColor getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("%s", getName());
    }
}
