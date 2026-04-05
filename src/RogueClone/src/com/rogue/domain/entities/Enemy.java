package com.rogue.domain.entities;

import com.rogue.domain.Position;
import java.util.Random;

public class Enemy extends Character {
    private EnemyType enemyType;
    private int hostilityRange;
    private boolean isVisible;
    private int specialCooldown;
    private Random random;

    public Enemy(EnemyType type) {
        super(type.getName(), type.getSymbol(),
                type.getHealth(), type.getDexterity(), type.getStrength());
        this.enemyType = type;
        this.hostilityRange = type.getHostilityRange();
        this.isVisible = true;
        this.specialCooldown = 0;
        this.random = new Random();
    }

    public boolean isHostileTo(Player player) {
        return position.distanceTo(player.getPosition()) <= hostilityRange;
    }

    public void performSpecialAbility(Player player) {
        String typeName = enemyType.name();

        if (typeName.equals("VAMPIRE")) {
            if (specialCooldown == 0 && random.nextDouble() < 0.3) {
                int drain = random.nextInt(10) + 5;
                player.setCurrentHealth(player.getCurrentHealth() - drain);
                heal(drain);
                specialCooldown = 3;
            }
        } else if (typeName.equals("GHOST")) {
            if (random.nextDouble() < 0.2) {
                isVisible = false;
                specialCooldown = 5;
            }
        } else if (typeName.equals("OGRE")) {
            if (specialCooldown == 0) {
                specialCooldown = 2;
            }
        } else if (typeName.equals("SNAKE_MAGE")) {
            if (random.nextDouble() < 0.25 && specialCooldown == 0) {
                player.applyTemporaryBuff("sleep", 1, 1);
                specialCooldown = 4;
            }
        }

        if (specialCooldown > 0) {
            specialCooldown--;
        }
    }

    public int getTreasureValue() {
        int baseValue = 10;
        baseValue += enemyType.getHealth() / 10;
        baseValue += enemyType.getStrength() * 2;
        baseValue += enemyType.getDexterity();
        return baseValue + random.nextInt(20);
    }

    public boolean canBeSeen() {
        return isVisible || specialCooldown == 0;
    }

    public EnemyType getEnemyType() { return enemyType; }
    public int getHostilityRange() { return hostilityRange; }
    public int getSpecialCooldown() { return specialCooldown; }
}