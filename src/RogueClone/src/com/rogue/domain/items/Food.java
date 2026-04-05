package com.rogue.domain.items;

import com.rogue.domain.entities.Player;

public class Food extends Consumable {
    public Food(String name, int healAmount) {
        super(name, ItemType.FOOD, '%', healAmount, "Heals " + healAmount + " HP");
    }

    @Override
    public void consume(Player player) {
        player.heal(value);
    }
}