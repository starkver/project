package org.example.domain.model.enums;

/** Типы предметов */
public enum ItemType {
    // type, subType, stackable, maxStack, displaySymbol
    FOOD("Еда", "food", false, 9, '!'),
    ELIXIR("Эликсир", "elixir", false, 9, '!'),
    SCROLL("Свиток", "scroll", false, 9, '?'),
    WEAPON("Оружие", "weapon", false, 9, ')'),
    TREASURE("Сокровище", "treasure", true, 1, '*'),
    KEY("Ключ", "key", false, 9, 'k'); // ключи хранятся в рюкзаке, максимум 9 каждого цвета

    private final String displayName;
    private final String subType; // для группировки и фильтрации
    private final boolean stackable; // можно ли складывать в стопки
    private final int maxStack; // макс. количество в одной стопке
    private final char displaySymbol; // символ для отображения на карте

    ItemType(
            String displayName, String subType, boolean stackable, int maxStack, char displaySymbol) {
        this.displayName = displayName;
        this.subType = subType;
        this.stackable = stackable;
        this.maxStack = maxStack;
        this.displaySymbol = displaySymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Проверить, является ли предмет сокровищем (не используется, только для счета)
    public boolean isTreasure() {
        return this == TREASURE;
    }

    // Проверить, является ли предмет ключом
    public boolean isKey() {
        return this == KEY;
    }
}
