package org.example.domain.service;

import org.example.domain.model.entity.Item;

/** Результат использования предмета. */
public class ItemUseResult {
    private final boolean success;
    private final String message;
    private final Item item;

    public ItemUseResult(boolean success, String message, Item item) {
        this.success = success;
        this.message = message;
        this.item = item;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Item getItem() {
        return item;
    }
}
