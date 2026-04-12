package org.example.domain.model.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.example.domain.model.enums.StatType;

/** Класс, представляющий игрока. Хранит рюкзак, экипированное оружие и активные эффекты. */
public class Player extends Character {
    private Backpack backpack;

    // Список активных временных эффектов от эликсиров
    private final List<TemporaryEffect> activeEffects;

    // Счётчик ходов
    private int turnsLived;

    public Player(int x, int y) {
        // Базовые характеристики игрока: здоровье 100, ловкость 10, сила 10
        super(x, y, 100, 100, 10, 10);

        this.backpack = new Backpack();
        this.activeEffects = new ArrayList<>();
        this.turnsLived = 0;
    }

    /** Получить текущий номер хода. */
    public int getTurnsLived() {
        return turnsLived;
    }

    /** Установить текущий номер хода (используется при загрузке сохранения). */
    public void setTurnsLived(int turns) {
        this.turnsLived = turns;
    }

    /** Увеличить счётчик ходов (вызывается в конце каждого хода игрока). */
    public void incrementTurn() {
        this.turnsLived++;
        tickEffects(); // обрабатываем истекшие эффекты
    }

    /** Добавить временный эффект от эликсира. */
    public void addTemporaryEffect(TemporaryEffect effect) {
        if (effect != null) {
            activeEffects.add(effect);
        }
    }

    /** Получить список активных эффектов. */
    public List<TemporaryEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }

    /** Обработать истекшие эффекты. */
    private void tickEffects() {
        Iterator<TemporaryEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            TemporaryEffect effect = iterator.next();

            // Проверяем, истёк ли эффект
            if (!effect.isActive(turnsLived)) {
                // Снимаем эффект
                removeEffect(effect);
                iterator.remove();
            }
        }
    }

    /** Снять эффект (уменьшить характеристики). */
    private void removeEffect(TemporaryEffect effect) {
        switch (effect.stat()) {
            case STRENGTH:
                setStrength(getStrength() - effect.bonusValue());
                break;

            case AGILITY:
                setAgility(getAgility() - effect.bonusValue());
                break;

            case MAX_HEALTH:
                int newMaxHealth = getMaxHealth() - effect.bonusValue();
                if (newMaxHealth < 1) {
                    newMaxHealth = 1;
                }
                setMaxHealth(newMaxHealth);

                // Если текущее здоровье стало больше нового максимума, уменьшаем его
                if (getHealth() > newMaxHealth) {
                    setHealth(newMaxHealth);

                    // Специальная обработка: если после снятия эликсира здоровья игрок умер,
                    // оставляем ему 1 HP (согласно заданию)
                    if (getHealth() <= 0) {
                        setHealth(1);
                        setAlive(true);
                    }
                }
                break;
        }
    }

    /** Получить эффективное значение силы (с учётом баффов). */
    public int getEffectiveStrength() {
        int baseStrength = getStrength();
        int bonus = 0;

        for (TemporaryEffect effect : activeEffects) {
            if (effect.isActive(turnsLived) && effect.stat() == StatType.STRENGTH) {
                bonus += effect.bonusValue();
            }
        }

        return baseStrength + bonus;
    }

    /** Получить эффективное значение ловкости (с учётом баффов). */
    public int getEffectiveAgility() {
        int baseAgility = getAgility();
        int bonus = 0;

        for (TemporaryEffect effect : activeEffects) {
            if (effect.isActive(turnsLived) && effect.stat() == StatType.AGILITY) {
                bonus += effect.bonusValue();
            }
        }

        return baseAgility + bonus;
    }

    /** Рассчитать урон, наносимый игроком. Учитывает эффективную силу и бонус оружия. */
    @Override
    public int calculateDamage() {
        int damage = getEffectiveStrength();
        if (isWeaponEquipped()) {
            damage += getCurrentWeapon().getDamageBonus();
        }
        return Math.max(1, damage); // Минимальный урон - 1
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
    }
}
