package com.rogue.domain.entities;

public enum EnemyType {
    ZOMBIE("Zombie", 'z', 50, 5, 12, 8, new java.awt.Color(0, 255, 0)),
    VAMPIRE("Vampire", 'v', 45, 18, 10, 10, new java.awt.Color(255, 0, 0)),
    GHOST("Ghost", 'g', 20, 16, 4, 12, new java.awt.Color(255, 255, 255)),
    OGRE("Ogre", 'O', 80, 4, 20, 6, new java.awt.Color(255, 255, 0)),
    MIMIC("Mimic", 'm', 60, 12, 8, 5, new java.awt.Color(255, 255, 255));

    private final String name;
    private final char symbol;
    private final int health;
    private final int dexterity;
    private final int strength;
    private final int hostilityRange;
    private final java.awt.Color color;

    EnemyType(String name, char symbol, int health, int dexterity,
              int strength, int hostilityRange, java.awt.Color color) {
        this.name = name;
        this.symbol = symbol;
        this.health = health;
        this.dexterity = dexterity;
        this.strength = strength;
        this.hostilityRange = hostilityRange;
        this.color = color;
    }

    public String getName() { return name; }
    public char getSymbol() { return symbol; }
    public int getHealth() { return health; }
    public int getDexterity() { return dexterity; }
    public int getStrength() { return strength; }
    public int getHostilityRange() { return hostilityRange; }
    public java.awt.Color getColor() { return color; }
}