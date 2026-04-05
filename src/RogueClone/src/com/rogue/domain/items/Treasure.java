package com.rogue.domain.items;

public class Treasure extends Item {
    private int value;

    public Treasure(int value) {
        super("Treasure", ItemType.TREASURE, '$');
        this.value = value;
    }

    @Override
    public String getDescription() {
        return "Worth " + value + " gold";
    }

    public int getValue() { return value; }
}