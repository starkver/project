package com.rogue.domain.items;

public enum ItemType {
    WEAPON(')'),
    FOOD('%'),
    ELIXIR('!'),
    SCROLL('?'),
    TREASURE('$');

    private final char symbol;

    ItemType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() { return symbol; }
}