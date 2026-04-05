package com.rogue.domain.items;

public class Weapon extends Item {
    private int strengthBonus;
    private int dexterityRequirement;

    public Weapon(String name, int strengthBonus, int dexterityRequirement) {
        super(name, ItemType.WEAPON, ')');
        this.strengthBonus = strengthBonus;
        this.dexterityRequirement = dexterityRequirement;
    }

    @Override
    public String getDescription() {
        return String.format("%s (Str +%d, Req Dex %d)", name, strengthBonus, dexterityRequirement);
    }

    public int getStrengthBonus() { return strengthBonus; }
    public int getDexterityRequirement() { return dexterityRequirement; }
}