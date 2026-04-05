package com.rogue.domain.combat;

import com.rogue.domain.entities.Character;
import com.rogue.domain.entities.Enemy;
import com.rogue.domain.entities.Player;
import java.util.Random;

public class CombatResolver {
    private static final Random random = new Random();

    public static CombatResult resolveAttack(Character attacker, Character defender) {
        // Step 1: Check if attacker is asleep
        if (attacker instanceof Player && isAsleep((Player) attacker)) {
            return CombatResult.SLEEP;
        }

        // Step 2: Calculate hit chance
        int hitChance = calculateHitChance(attacker.getDexterity(), defender.getDexterity());

        // Special: First hit on vampire always misses
        if (defender instanceof Enemy && ((Enemy) defender).getEnemyType() == com.rogue.domain.entities.EnemyType.VAMPIRE) {
            if (((Enemy) defender).getSpecialCooldown() == 0) {
                return CombatResult.MISS;
            }
        }

        if (random.nextInt(100) >= hitChance) {
            return CombatResult.MISS;
        }

        // Step 3: Calculate damage
        int damage = calculateDamage(attacker);

        // Step 4: Apply damage
        defender.applyDamage(damage);

        // Step 5: Apply special effects
        if (attacker instanceof Enemy) {
            ((Enemy) attacker).performSpecialAbility((Player) defender);
        }

        return new CombatResult(damage, !defender.isAlive());
    }

    private static int calculateHitChance(int attackerDex, int defenderDex) {
        int chance = 50 + (attackerDex - defenderDex) * 2;
        return Math.min(95, Math.max(5, chance));
    }

    private static int calculateDamage(Character attacker) {
        int baseDamage = attacker.getEffectiveStrength();
        // Random variance ±20%
        int variance = random.nextInt(baseDamage / 5 * 2) - baseDamage / 5;
        return Math.max(1, baseDamage + variance);
    }

    private static boolean isAsleep(Player player) {
        // Check if player has sleep debuff
        return false; // Simplified for now
    }

    public static class CombatResult {
        private final int damage;
        private final boolean isKillingBlow;
        private final boolean isMiss;
        private final boolean isSleep;

        public CombatResult(int damage, boolean isKillingBlow) {
            this.damage = damage;
            this.isKillingBlow = isKillingBlow;
            this.isMiss = false;
            this.isSleep = false;
        }

        private CombatResult(String type) {
            this.damage = 0;
            this.isKillingBlow = false;
            this.isMiss = type.equals("MISS");
            this.isSleep = type.equals("SLEEP");
        }

        public static final CombatResult MISS = new CombatResult("MISS");
        public static final CombatResult SLEEP = new CombatResult("SLEEP");

        public int getDamage() { return damage; }
        public boolean isKillingBlow() { return isKillingBlow; }
        public boolean isMiss() { return isMiss; }
        public boolean isSleep() { return isSleep; }
    }
}