package com.rogue.domain.entities;

import com.rogue.domain.Position;
import com.rogue.domain.combat.Buff;
import com.rogue.domain.items.*;
import java.util.*;

public class Player extends Character {
    private Inventory inventory;
    private int gold;
    private int level;
    private int experience;
    private Map<ItemType, List<Item>> itemStacks;
    private List<Buff> activeBuffs;

    public Player() {
        super("Hero", '@', 100, 10, 10);
        this.inventory = new Inventory();
        this.gold = 0;
        this.level = 1;
        this.experience = 0;
        this.itemStacks = new HashMap<>();
        this.activeBuffs = new ArrayList<>();

        // Initialize item stacks
        for (ItemType type : ItemType.values()) {
            itemStacks.put(type, new ArrayList<>());
        }
    }

    public boolean pickUpItem(Item item) {
        List<Item> stack = itemStacks.get(item.getType());

        if (item.getType() == ItemType.TREASURE) {
            gold += ((Treasure) item).getValue();
            return true;
        }

        if (stack.size() < 9) {
            stack.add(item);
            return true;
        }
        return false;
    }

    public Item useItem(ItemType type, int slot) {
        List<Item> stack = itemStacks.get(type);
        if (slot < 0 || slot >= stack.size()) return null;

        Item item = stack.remove(slot);

        if (item instanceof Consumable) {
            ((Consumable) item).consume(this);
        } else if (item instanceof Weapon && type == ItemType.WEAPON) {
            // Drop current weapon if exists
            if (weapon != null) {
                stack.add(weapon);
            }
            weapon = (Weapon) item;
        }

        return item;
    }

    public List<Item> getItemStack(ItemType type) {
        return itemStacks.get(type);
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public void updateBuffs() {
        activeBuffs.removeIf(buff -> {
            if (buff.isExpired()) {
                removeStatBuff(buff.getStat(), buff.getValue());
                return true;
            }
            buff.tick();
            return false;
        });
    }

    private void removeStatBuff(String stat, int value) {
        switch (stat) {
            case "dexterity":
                dexterity -= value;
                break;
            case "strength":
                strength -= value;
                break;
        }
    }

    public void gainExperience(int amount) {
        experience += amount;
        if (experience >= level * 100) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        experience = 0;
        maxHealth += 10;
        currentHealth = maxHealth;
        strength += 2;
        dexterity += 1;
    }

    // Getters
    public int getGold() { return gold; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }

    // Inner Inventory class
    public class Inventory {
        private Map<ItemType, Integer> counts;

        public Inventory() {
            counts = new HashMap<>();
        }

        public int getCount(ItemType type) {
            return counts.getOrDefault(type, 0);
        }
    }
}