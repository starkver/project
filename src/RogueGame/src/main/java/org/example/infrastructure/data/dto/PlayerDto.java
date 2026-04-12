package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/** DTO для игрока. Содержит полную информацию о состоянии игрока. */
public class PlayerDto {
    // Позиция
    private int x;
    private int y;

    // Характеристики
    private int health;
    private int maxHealth;
    private int agility;
    private int strength;
    private boolean isAlive;
    private int turnsLived;

    // Оружие и рюкзак
    private String currentWeapon; // подтип текущего оружия (SHORT_SWORD и т.д.)
    private BackpackDto backpack;

    // Активные эффекты
    private List<TemporaryEffectDto> activeEffects;

    public PlayerDto() {
        this.activeEffects = new ArrayList<>();
    }

    // Геттеры и сеттеры
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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getTurnsLived() {
        return turnsLived;
    }

    public void setTurnsLived(int turnsLived) {
        this.turnsLived = turnsLived;
    }

    public String getCurrentWeapon() {
        return currentWeapon;
    }

    public void setCurrentWeapon(String currentWeapon) {
        this.currentWeapon = currentWeapon;
    }

    public BackpackDto getBackpack() {
        return backpack;
    }

    public void setBackpack(BackpackDto backpack) {
        this.backpack = backpack;
    }

    public List<TemporaryEffectDto> getActiveEffects() {
        return activeEffects;
    }

    public void setActiveEffects(List<TemporaryEffectDto> activeEffects) {
        this.activeEffects = activeEffects;
    }
}
