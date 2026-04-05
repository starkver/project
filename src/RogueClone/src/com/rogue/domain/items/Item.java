package com.rogue.domain.items;

public abstract class Item {
    protected String name;
    protected ItemType type;
    protected char symbol;

    public Item(String name, ItemType type, char symbol) {
        this.name = name;
        this.type = type;
        this.symbol = symbol;
    }

    public String getName() { return name; }
    public ItemType getType() { return type; }
    public char getSymbol() { return symbol; }

    public abstract String getDescription();
}