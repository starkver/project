package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;

/**
 * Сокровище — не используется, только накапливается для подсчёта очков. Подтипы: SILVER_COIN,
 * GOLD_COIN, SILVER_BAR, GOLD_BAR, GEM
 */
public class Treasure extends Item {
    public Treasure(ItemSubType subType) {
        super(subType.getDisplayName(), ItemType.TREASURE, subType, subType.getValue());
    }
}
