package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;
import org.example.domain.model.enums.StatType;

/**
 * Эликсир — временно повышает одну из характеристик. Подтипы: ELIXIR_STRENGTH, ELIXIR_AGILITY,
 * ELIXIR_MAX_HEALTH
 */
public class Elixir extends Item {

    public Elixir(ItemSubType subType) {
        super(
                subType.getDisplayName(),
                ItemType.ELIXIR,
                subType,
                subType.getValue()); // value = величина бонуса
    }

    /**
     * Применить эликсир к игроку.
     *
     * @return созданный временный эффект
     */
    public TemporaryEffect applyTo(Player player, int currentTurn) {
        StatType stat = getStatType();
        int bonus = getValue();
        int duration = getDurationTurns();

        // Создаём временный эффект
        TemporaryEffect effect =
                new TemporaryEffect(generateEffectId(stat), stat, bonus, duration, currentTurn);

        // Применяем эффект немедленно
        applyEffectToPlayer(player, effect);

        // Добавляем эффект в список активных эффектов игрока
        player.addTemporaryEffect(effect);

        return effect;
    }

    /** Применить эффект к игроку (при добавлении или снятии). */
    private void applyEffectToPlayer(Player player, TemporaryEffect effect) {
        int multiplier = 1;
        int value = effect.bonusValue() * multiplier;

        switch (effect.stat()) {
            case STRENGTH:
                player.setStrength(player.getStrength() + value);
                break;

            case AGILITY:
                player.setAgility(player.getAgility() + value);
                break;

            case MAX_HEALTH:
                // При применении увеличиваем максимум и текущее здоровье
                player.increaseMaxHealth(value);
                break;
        }
    }

    /** Получить тип характеристики, которую повышает эликсир. */
    private StatType getStatType() {
        return switch (subType) {
            case ELIXIR_AGILITY -> StatType.AGILITY;
            case ELIXIR_MAX_HEALTH -> StatType.MAX_HEALTH;
            default -> StatType.STRENGTH;
        };
    }

    /** Получить длительность действия в ходах. */
    private int getDurationTurns() {
        return switch (subType) {
            case ELIXIR_STRENGTH, ELIXIR_AGILITY -> 10; // 10 ходов
            case ELIXIR_MAX_HEALTH -> 15; // 15 ходов
            default -> 5;
        };
    }

    /** Сгенерировать уникальный ID для эффекта. */
    private String generateEffectId(StatType stat) {
        return String.format(
                "elixir_%s_%d_%d", stat.name().toLowerCase(), getValue(), System.nanoTime());
    }

    @Override
    public String toString() {
        return String.format(
                "%s (+%d %s на %d ходов)",
                getName(), getValue(), getStatType().name().toLowerCase(), getDurationTurns());
    }
}
