package org.example.domain.model.enums;

/**
 * Подтип предмета — конкретная разновидность внутри категории. Используется для дифференциации
 * эффектов и отображения.
 *
 * <p>Значения соответствуют заданию: - Еда: восстанавливает здоровье (хлеб 10, мясо 25, яблоко 15)
 * - Эликсиры: временные бонусы (сила +3 на 10 ходов, ловкость +3 на 10 ходов, здоровье +5 на 15
 * ходов) - Свитки: постоянные бонусы (сила +1, ловкость +1, здоровье +2) - Оружие: бонус к урону
 * (короткий меч +2, длинный меч +6, топор +4) - Сокровища: стоимость (серебряная монета 1, золотая
 * монета 10, слиток серебра 5, слиток золота 50, драг. камень 100) - Ключи: открывают двери
 * соответствующего цвета
 */
public enum ItemSubType {
    BREAD("Хлеб", 10, 0, false), // Восстанавливает 10 HP
    MEAT("Мясо", 25, 0, false), // Восстанавливает 25 HP
    APPLE("Яблоко", 15, 0, false), // Восстанавливает 15 HP

    ELIXIR_STRENGTH("Эликсир силы", 3, 10, true), // +3 силы на 10 ходов
    ELIXIR_AGILITY("Эликсир ловкости", 3, 10, true), // +3 ловкости на 10 ходов
    ELIXIR_MAX_HEALTH("Эликсир здоровья", 5, 15, true), // +5 макс. здоровья на 15 ходов

    SCROLL_STRENGTH("Свиток силы", 1, -1, false), // +1 силы навсегда
    SCROLL_AGILITY("Свиток ловкости", 1, -1, false), // +1 ловкости навсегда
    SCROLL_MAX_HEALTH("Свиток здоровья", 2, -1, false), // +2 макс. здоровья навсегда

    SHORT_SWORD("Короткий меч", 2, -1, false), // +2 к урону
    LONG_SWORD("Длинный меч", 6, -1, false), // +6 к урону
    AXE("Топор", 4, -1, false), // +4 к урону

    SILVER_COIN("Серебряная монета", 1, 0, false), // Стоимость 1
    GOLD_COIN("Золотая монета", 10, 0, false), // Стоимость 10
    SILVER_BAR("Серебряный слиток", 5, 0, false), // Стоимость 5
    GOLD_BAR("Золотой слиток", 50, 0, false), // Стоимость 50
    GEM("Драгоценный камень", 100, 0, false), // Стоимость 100

    KEY_RED("Красный ключ", 1, 0, false),
    KEY_BLUE("Синий ключ", 1, 0, false),
    KEY_YELLOW("Жёлтый ключ", 1, 0, false),
    KEY_GREEN("Зелёный ключ", 1, 0, false),
    KEY_PURPLE("Фиолетовый ключ", 1, 0, false);

    private final String displayName;
    private final int
            value; // Для еды: лечение, для эликсиров/свитков: бонус, для оружия: бонус к урону, для
    // сокровищ: стоимость, для ключей: не используется
    private final int
            durationTurns; // >0 = временный эффект, -1 = постоянный, 0 = мгновенный/неприменимо
    private final boolean isTemporary; // true для эликсиров, false для остальных

    ItemSubType(String displayName, int value, int durationTurns, boolean isTemporary) {
        this.displayName = displayName;
        this.value = value;
        this.durationTurns = durationTurns;
        this.isTemporary = isTemporary;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }

    /** Является ли подтип эликсиром (временный эффект). */
    public boolean isElixir() {
        return durationTurns > 0;
    }

    /** Является ли подтип свитком (постоянный эффект). */
    public boolean isScroll() {
        return durationTurns == -1 && !isWeapon() && !isTreasure() && !isKey();
    }

    /** Является ли подтип оружием. */
    public boolean isWeapon() {
        return this == SHORT_SWORD || this == LONG_SWORD || this == AXE;
    }

    /** Является ли подтип сокровищем. */
    public boolean isTreasure() {
        return this == SILVER_COIN
                || this == GOLD_COIN
                || this == SILVER_BAR
                || this == GOLD_BAR
                || this == GEM;
    }

    /** Является ли подтип едой. */
    public boolean isFood() {
        return this == BREAD || this == MEAT || this == APPLE;
    }

    /** Является ли подтип ключом. */
    public boolean isKey() {
        return this == KEY_RED
                || this == KEY_BLUE
                || this == KEY_YELLOW
                || this == KEY_GREEN
                || this == KEY_PURPLE;
    }

    /** Получить цвет ключа для подтипа ключа. */
    public KeyColor getKeyColor() {
        if (!isKey()) return null;
        return switch (this) {
            case KEY_RED -> KeyColor.RED;
            case KEY_BLUE -> KeyColor.BLUE;
            case KEY_YELLOW -> KeyColor.YELLOW;
            case KEY_GREEN -> KeyColor.GREEN;
            case KEY_PURPLE -> KeyColor.PURPLE;
            default -> null;
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(displayName);
        if (isFood()) {
            sb.append(" (+").append(value).append(" HP)");
        } else if (isElixir()) {
            sb.append(" (+").append(value).append(" на ").append(durationTurns).append(" ходов)");
        } else if (isScroll()) {
            sb.append(" (+").append(value).append(" постоянно)");
        } else if (isWeapon()) {
            sb.append(" (урон +").append(value).append(")");
        } else if (isTreasure()) {
            sb.append(" (").append(value).append(" монет)");
        } else if (isKey()) {
            sb.append(" (открывает ").append(getKeyColor().getDisplayName()).append(" двери)");
        }
        return sb.toString();
    }
}
