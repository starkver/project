package org.example.domain.model.entity;

/**
 * Базовый класс для всех персонажей (игрок и монстры) Хранит характеристики и базовую логику:
 * здоровье, перемещение, урон
 */
public abstract class Character {
    private int x;
    private int y;
    private int health;
    private int maxHealth;
    private int agility;
    private int strength;
    private boolean isAlive;

    // Текущее экипированное оружие (только для игрока, но храним в базовом классе
    // для удобства доступа из сервисов)
    private Weapon currentWeapon;

    protected Character(int x, int y, int health, int maxHealth, int agility, int strength) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.agility = agility;
        this.strength = strength;
        this.health = Math.min(health, maxHealth);
        this.isAlive = this.health > 0;
        this.currentWeapon = null;
    }

    /** Получить урон. */
    public void takeDamage(int amount) {
        this.health = Math.max(0, this.health - amount);
        if (this.health == 0) {
            this.isAlive = false;
        }
    }

    /** Исцелить персонажа. */
    public void heal(int amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
        this.isAlive = this.health > 0;
    }

    /** Рассчитать урон, наносимый персонажем. База: сила + бонус оружия (если есть) */
    public int calculateDamage() {
        int damage = this.strength;
        if (currentWeapon != null) {
            damage += currentWeapon.getDamageBonus();
        }
        return Math.max(1, damage); // Минимальный урон - 1
    }

    /** Увеличить максимальное здоровье (и текущее на ту же величину). */
    public void increaseMaxHealth(int amount) {
        this.maxHealth += amount;
        this.health += amount;
    }

    /** Уменьшить максимальное здоровье (с защитой от падения ниже 1). */
    public void decreaseMaxHealth(int amount) {
        this.maxHealth = Math.max(1, this.maxHealth - amount);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        if (this.health <= 0) {
            this.isAlive = false;
        }
    }

    /** Увеличить ловкость. */
    public void increaseAgility(int amount) {
        this.agility += amount;
    }

    /** Увеличить силу. */
    public void increaseStrength(int amount) {
        this.strength += amount;
    }

    /** Экипировать оружие. */
    public Weapon equipWeapon(Weapon weapon) {
        Weapon oldWeapon = this.currentWeapon;
        this.currentWeapon = weapon;
        return oldWeapon;
    }

    /** Снять текущее оружие. */
    public Weapon unequipWeapon() {
        Weapon removed = this.currentWeapon;
        this.currentWeapon = null;
        return removed;
    }

    /** Проверить, экипировано ли оружие. */
    public boolean isWeaponEquipped() {
        return currentWeapon != null;
    }

    /** Переместить в абсолютные координаты. */
    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    /** Расстояние до другой точки (манхэттенское). */
    public int distanceTo(int targetX, int targetY) {
        return Math.abs(this.x - targetX) + Math.abs(this.y - targetY);
    }

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
        this.health = Math.min(health, maxHealth);
        this.isAlive = this.health > 0;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
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

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public void setCurrentWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }
}
