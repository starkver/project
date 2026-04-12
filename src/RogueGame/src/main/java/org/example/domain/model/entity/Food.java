package org.example.domain.model.entity;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.ItemType;

/**
 * Еда — восстанавливает здоровье мгновенно. Подтипы: BREAD, MEAT, APPLE Не может превысить
 * максимальное здоровье игрока.
 */
public class Food extends Item {

    public Food(ItemSubType subType) {
        super(
                subType.getDisplayName(),
                ItemType.FOOD,
                subType,
                subType.getValue()); // value = количество восстанавливаемого здоровья
    }

    /**
     * Применить еду к игроку (лечение).
     *
     * @return количество реально восстановленного здоровья (с учётом максимума)
     */
    public int applyTo(Player player) {
        int healAmount = getValue();
        int oldHealth = player.getHealth();

        // Лечим игрока (метод heal сам не даст превысить максимум)
        player.heal(healAmount);

        // Возвращаем, сколько реально восстановили
        return player.getHealth() - oldHealth;
    }

    /**
     * Проверить, есть ли смысл использовать эту еду (игрок не на полном здоровье или еда даст
     * прирост).
     */
    public boolean isUseful(Player player) {
        return player.getHealth() < player.getMaxHealth();
    }

    /** Получить количество восстанавливаемого здоровья. */
    public int getHealAmount() {
        return getValue();
    }

    @Override
    public String toString() {
        return String.format("%s (восстанавливает %d HP)", getName(), getValue());
    }
}
