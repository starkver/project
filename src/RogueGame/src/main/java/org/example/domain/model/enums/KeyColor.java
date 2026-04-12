package org.example.domain.model.enums;

/** Цвета ключей и дверей. Используется для системы ключей-дверей (аналог DOOM). */
public enum KeyColor {
    RED("Красный", 'R'),
    BLUE("Синий", 'B'),
    YELLOW("Жёлтый", 'Y'),
    GREEN("Зелёный", 'G'),
    PURPLE("Фиолетовый", 'P');

    private final String displayName;

    KeyColor(String displayName, char symbol) {
        this.displayName = displayName;
    }

    /** Возвращает отображаемое имя цвета. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получает подтип предмета для данного цвета ключа.
     *
     * @return соответствующий подтип ключа
     */
    public ItemSubType toItemSubType() {
        return switch (this) {
            case RED -> ItemSubType.KEY_RED;
            case BLUE -> ItemSubType.KEY_BLUE;
            case YELLOW -> ItemSubType.KEY_YELLOW;
            case GREEN -> ItemSubType.KEY_GREEN;
            case PURPLE -> ItemSubType.KEY_PURPLE;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
