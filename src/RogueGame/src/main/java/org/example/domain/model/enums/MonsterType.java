package org.example.domain.model.enums;

/**
 * Типы монстров с базовыми характеристиками. Соответствует заданию: - Зомби (зелёный z): низкая
 * ловкость, средняя сила, высокое здоровье - Вампир (красная v): высокая ловкость, средняя сила,
 * высокое здоровье - Привидение (белый g): высокая ловкость, низкая сила, низкое здоровье - Огр
 * (жёлтый O): очень высокая сила, очень высокое здоровье, низкая ловкость - Змей-маг (белая s):
 * очень высокая ловкость, средняя сила, среднее здоровье - Мимик (белая m): высокая ловкость,
 * низкая сила, высокое здоровье
 */
public enum MonsterType {
    // symbol, ansiColor, health, agility, strength, hostilityRange, treasureBase, description
    ZOMBIE('z', "\u001B[32m", 80, 3, 5, 4, 10, "Зомби - медленный, но живучий"),
    VAMPIRE(
            'v',
            "\u001B[31m",
            70,
            7,
            5,
            8,
            15,
            "Вампир - первый удар всегда промах, крадёт макс. здоровье"),
    GHOST('g', "\u001B[37m", 30, 8, 2, 3, 5, "Привидение - телепортируется и становится невидимым"),
    OGRE('O', "\u001B[33m", 120, 2, 10, 6, 20, "Огр - очень сильный, после атаки отдыхает"),
    SNAKE_MAGE('s', "\u001B[37m", 50, 9, 4, 7, 25, "Змей-маг - ходит по диагонали, может усыпить"),
    MIMIC('m', "\u001B[37m", 100, 9, 2, 3, 30, "Мимик - притворяется предметом");

    private final char symbol;
    private final String ansiColor;
    private final int baseHealth;
    private final int baseAgility;
    private final int baseStrength;
    private final int hostilityRange; // дистанция начала преследования
    private final int treasureBase; // базовое количество сокровищ
    private final String description; // описание для справки

    MonsterType(
            char symbol,
            String ansiColor,
            int health,
            int agility,
            int strength,
            int hostility,
            int treasure,
            String description) {
        this.symbol = symbol;
        this.ansiColor = ansiColor;
        this.baseHealth = health;
        this.baseAgility = agility;
        this.baseStrength = strength;
        this.hostilityRange = hostility;
        this.treasureBase = treasure;
        this.description = description;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getAnsiColor() {
        return ansiColor;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public int getBaseAgility() {
        return baseAgility;
    }

    public int getBaseStrength() {
        return baseStrength;
    }

    public int getHostilityRange() {
        return hostilityRange;
    }

    public int getTreasureBase() {
        return treasureBase;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format(
                "%s (%c) HP:%d Ловк:%d Сил:%d", name(), symbol, baseHealth, baseAgility, baseStrength);
    }
}
