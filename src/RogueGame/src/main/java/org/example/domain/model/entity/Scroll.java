package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;

/**
 * Свиток — постоянно повышает одну из характеристик. Подтипы: SCROLL_STRENGTH, SCROLL_AGILITY,
 * SCROLL_MAX_HEALTH. В отличие от эликсиров, эффект свитка постоянный.
 */
public class Scroll extends Item {

    public Scroll(ItemSubType subType) {
        super(
                subType.getDisplayName(),
                ItemType.SCROLL,
                subType,
                subType.getValue()); // value = величина повышения
    }

    /** Применить свиток к игроку (постоянное улучшение). */
    public void applyTo(Player player) {
        int bonus = getValue();

        switch (subType) {
            case SCROLL_STRENGTH:
                player.increaseStrength(bonus);
                break;

            case SCROLL_AGILITY:
                player.increaseAgility(bonus);
                break;

            case SCROLL_MAX_HEALTH:
                // increaseMaxHealth увеличивает и максимум, и текущее здоровье
                player.increaseMaxHealth(bonus);
                break;

            default:
        }
    }

    /** Получить тип улучшаемой характеристики. */
    public String getStatName() {
        return switch (subType) {
            case SCROLL_STRENGTH -> "сила";
            case SCROLL_AGILITY -> "ловкость";
            case SCROLL_MAX_HEALTH -> "максимальное здоровье";
            default -> "неизвестно";
        };
    }

    @Override
    public String toString() {
        return String.format("%s (+%d %s)", getName(), getValue(), getStatName());
    }
}
