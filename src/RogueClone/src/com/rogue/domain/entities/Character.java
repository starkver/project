package com.rogue.domain.entities;

import com.rogue.domain.Position;
import com.rogue.domain.items.Weapon;
import java.util.HashMap;
import java.util.Map;

public abstract class Character {
    protected String name;
    protected char symbol;
    protected Position position;
    protected int maxHealth;
    protected int currentHealth;
    protected int dexterity;
    protected int strength;
    protected Weapon weapon;
    protected Map<String, Integer> temporaryBuffs;
    protected Map<String, Integer> permanentBuffs;

    public Character(String name, char symbol, int maxHealth, int dexterity, int strength) {
        this.name = name;
        this.symbol = symbol;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.dexterity = dexterity;
        this.strength = strength;
        this.temporaryBuffs = new HashMap<>();
        this.permanentBuffs = new HashMap<>();
    }

    public void applyDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
    }

    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public void applyTemporaryBuff(String stat, int value, int duration) {
        temporaryBuffs.put(stat + "_" + System.currentTimeMillis(), value);
        applyStatBuff(stat, value);
    }

    public void applyPermanentBuff(String stat, int value) {
        permanentBuffs.put(stat, permanentBuffs.getOrDefault(stat, 0) + value);
        applyStatBuff(stat, value);
    }

    private void applyStatBuff(String stat, int value) {
        switch (stat) {
            case "health":
                maxHealth += value;
                currentHealth += value;
                break;
            case "dexterity":
                dexterity += value;
                break;
            case "strength":
                strength += value;
                break;
        }
    }

    public int getEffectiveStrength() {
        int total = strength;
        if (weapon != null) {
            total += weapon.getStrengthBonus();
        }
        return total;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    // Getters and setters
    public String getName() { return name; }
    public char getSymbol() { return symbol; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int health) { this.currentHealth = health; }
    public int getDexterity() { return dexterity; }
    public int getStrength() { return strength; }
    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }
}