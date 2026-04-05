package com.rogue.domain.items;

import com.rogue.domain.entities.Player;

public abstract class Consumable extends Item {
    protected int value;
    protected String effect;

    public Consumable(String name, ItemType type, char symbol, int value, String effect) {
        super(name, type, symbol);
        this.value = value;
        this.effect = effect;
    }

    public abstract void consume(Player player);

    @Override
    public String getDescription() {
        return String.format("%s (%s)", name, effect);
    }
}