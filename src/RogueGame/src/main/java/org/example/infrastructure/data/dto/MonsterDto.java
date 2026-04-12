package org.example.infrastructure.data.dto;

/** DTO для монстра. Использует String для MonsterType для безопасной сериализации. */
public class MonsterDto {
    private int x;
    private int y;
    private String type; // ZOMBIE, VAMPIRE, GHOST, OGRE, SNAKE_MAGE, MIMIC
    private int health;
    private int maxHealth;
    private int agility;
    private int strength;
    private boolean isAlive;

    // Специфичные состояния
    private boolean isResting; // для огра
    private boolean isInvisible; // для призрака/мимика
    private boolean firstAttackDone; // для вампира
    private int turnsSinceLastAttack; // для огра

    public MonsterDto() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isResting() {
        return isResting;
    }

    public void setResting(boolean resting) {
        isResting = resting;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean invisible) {
        isInvisible = invisible;
    }

    public boolean isFirstAttackDone() {
        return firstAttackDone;
    }

    public void setFirstAttackDone(boolean firstAttackDone) {
        this.firstAttackDone = firstAttackDone;
    }

    public int getTurnsSinceLastAttack() {
        return turnsSinceLastAttack;
    }

    public void setTurnsSinceLastAttack(int turnsSinceLastAttack) {
        this.turnsSinceLastAttack = turnsSinceLastAttack;
    }
}
