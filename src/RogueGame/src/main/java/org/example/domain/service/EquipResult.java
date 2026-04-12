package org.example.domain.service;

import org.example.domain.model.entity.Weapon;

/** Результат экипировки оружия. */
public class EquipResult {
    private final boolean success;
    private final String message;
    private final Weapon newWeapon;
    private final Weapon oldWeapon;

    public EquipResult(boolean success, String message, Weapon newWeapon, Weapon oldWeapon) {
        this.success = success;
        this.message = message;
        this.newWeapon = newWeapon;
        this.oldWeapon = oldWeapon;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
