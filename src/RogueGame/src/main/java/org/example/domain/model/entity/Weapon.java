package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;

/** Оружие — добавляет бонус к силе при расчёте урона. Подтипы: SHORT_SWORD, LONG_SWORD, AXE. */
public class Weapon extends Item {
    public Weapon(ItemSubType subType) {
        super(subType.getDisplayName(), ItemType.WEAPON, subType, subType.getValue());
    }

    // Бонус к урону
    public int getDamageBonus() {
        return subType.getValue();
    }
}
