package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;

/** Базовый класс для всех предметов. Хранит общие данные: название, тип, подтип, стоимость */
public abstract class Item {
    protected final String name;
    protected final ItemType type;
    protected final ItemSubType subType;
    protected final int value;

    protected Item(String name, ItemType type, ItemSubType subType, int value) {
        this.name = name;
        this.type = type;
        this.subType = subType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public ItemSubType getSubType() {
        return subType;
    }

    public int getValue() {
        return value;
    }
}
