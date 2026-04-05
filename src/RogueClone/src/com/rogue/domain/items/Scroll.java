package com.rogue.domain.items;

import com.rogue.domain.entities.Player;

public class Scroll extends Consumable {
    private String stat;

    public Scroll(String name, String stat, int bonus) {
        super(name, ItemType.SCROLL, '?', bonus, "Permanently increases " + stat + " by " + bonus);
        this.stat = stat;
    }

    @Override
    public void consume(Player player) {
        player.applyPermanentBuff(stat, value);
    }
}