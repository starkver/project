package com.rogue.domain.items;

import com.rogue.domain.entities.Player;

public class Potion extends Consumable {
    private String stat;

    public Potion(String name, String stat, int bonus, int duration) {
        super(name, ItemType.ELIXIR, '!', bonus, "Temporary +" + bonus + " " + stat + " for " + duration + " turns");
        this.stat = stat;
    }

    @Override
    public void consume(Player player) {
        player.applyTemporaryBuff(stat, value, 10);
    }
}