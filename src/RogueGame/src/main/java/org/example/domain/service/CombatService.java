package org.example.domain.service;

import java.util.Random;
import org.example.domain.model.entity.*;
import org.example.domain.model.entity.Character;
import org.example.domain.model.enums.*;

/**
 * Сервис отвечающий за боевую механику игры. Обрабатывает атаки, расчет попадания, урон и
 * специальные эффекты монстров.
 */
public class CombatService {

    private final Random random = new Random();
    private DifficultyBalanceService difficultyBalanceService;
    private long currentSessionId;

    /** Конструктор с внедрением DifficultyBalanceService. */
    public CombatService(DifficultyBalanceService difficultyBalanceService) {
        this.difficultyBalanceService = difficultyBalanceService;
    }

    /** Устанавливает ID текущей сессии для регистрации метрик. */
    public void setCurrentSessionId(long sessionId) {
        this.currentSessionId = sessionId;
    }

    /**
     * Обрабатывает атаку атакующего на цель. Учитывает тип атакующего (игрок или монстр) и
     * специальные способности.
     *
     * @param attacker атакующий персонаж
     * @param target цель атаки
     * @return результат боевого взаимодействия
     */
    public CombatResult processAttack(Character attacker, Character target) {
        // Базовая проверка: цель мертва?
        if (!target.isAlive()) {
            return new CombatResult(
                    false,
                    0,
                    false,
                    "Цель уже мертва",
                    attacker.getHealth(),
                    target.getHealth(),
                    getMonsterType(target));
        }

        // Запоминаем здоровье до атаки для расчёта статов
        int oldTargetHealth = target.getHealth();

        // Шаг 1: Проверка на попадание
        boolean hit = calculateHit(attacker, target);

        // Применяем особые правила для первого удара по вампиру
        if (target instanceof Monster && ((Monster) target).getMonsterType() == MonsterType.VAMPIRE) {
            Monster vampire = (Monster) target;
            if (!vampire.isFirstAttackDone()) {
                hit = false; // Первый удар по вампиру - всегда промах
                vampire.markFirstAttackDone(); // Отмечаем, что первый удар был
            }
        }

        if (!hit) {
            // Регистрируем промах в системе балансировки
            if (attacker instanceof Player && difficultyBalanceService != null) {
                difficultyBalanceService.registerDamageDealt(0, false);
            }
            return new CombatResult(
                    false,
                    0,
                    false,
                    "Промах",
                    attacker.getHealth(),
                    target.getHealth(),
                    getMonsterType(target));
        }

        // Шаг 2: Расчет урона
        int damage = calculateDamage(attacker, target);

        // Шаг 3: Применение специальных эффектов атакующего (для монстров)
        String specialEffect = null;
        if (attacker instanceof Monster) {
            specialEffect = applyAttackerSpecialEffect((Monster) attacker, target, damage);
        }

        // Шаг 4: Применение урона к цели
        target.takeDamage(damage);

        // Реальное полученное урона (с учётом возможного лечения и т.д.)
        int actualDamageTaken = oldTargetHealth - target.getHealth();

        // Шаг 5: Регистрация урона в системе балансировки
        if (attacker instanceof Player && difficultyBalanceService != null) {
            // Игрок нанёс урон
            difficultyBalanceService.registerDamageDealt(actualDamageTaken, true);
        }

        if (target instanceof Player && difficultyBalanceService != null) {
            // Игрок получил урон
            difficultyBalanceService.registerDamageTaken(
                    actualDamageTaken, target.getHealth(), target.getMaxHealth());
        }

        // Шаг 6: Проверка специальных эффектов, срабатывающих при получении урона
        if (target instanceof Monster) {
            applyOnHitEffects((Monster) target, attacker, actualDamageTaken);
        }

        boolean targetDefeated = !target.isAlive();

        // Шаг 7: Если цель - игрок и она умерла, обрабатываем смерть
        if (target instanceof Player && targetDefeated) {
            // Дополнительная логика смерти игрока (будет обработана в GameController)
        }

        // Шаг 8: Если цель - вампир, который получил урон, обновляем состояние
        if (target instanceof Monster
                && ((Monster) target).getMonsterType() == MonsterType.VAMPIRE
                && hit) {
            // Вампир уже получил урон, ничего дополнительно не делаем
        }

        // Шаг 9: Обработка контратаки огра
        if (attacker instanceof Player
                && target instanceof Monster
                && ((Monster) target).getMonsterType() == MonsterType.OGRE
                && target.isAlive()
                && ((Monster) target).canAttack()) {

            // Огр может контратаковать с 50% шансом
            if (random.nextDouble() < 0.5) {
                // Контратака будет обработана отдельно в MovementService
                specialEffect =
                        (specialEffect == null ? "" : specialEffect + " ") + "Огр готовится контратаковать!";
            }
        }

        return new CombatResult(
                true,
                actualDamageTaken,
                targetDefeated,
                specialEffect,
                attacker.getHealth(),
                target.getHealth(),
                getMonsterType(target));
    }

    /**
     * Расчет попадания на основе ловкости атакующего и цели. Формула: шанс попадания = (ловкость
     * атакующего) / (ловкость атакующего + ловкость цели) * 100%
     */
    private boolean calculateHit(Character attacker, Character target) {
        int attackerAgility =
                attacker instanceof Player
                        ? ((Player) attacker).getEffectiveAgility()
                        : attacker.getAgility();
        int targetAgility =
                target instanceof Player ? ((Player) target).getEffectiveAgility() : target.getAgility();

        // Избегаем деления на ноль
        if (attackerAgility + targetAgility == 0) {
            return true; // Если у обоих ловкость 0, всегда попадание
        }

        double hitChance = (double) attackerAgility / (attackerAgility + targetAgility);

        return random.nextDouble() < hitChance;
    }

    /**
     * Расчет урона на основе силы атакующего и оружия (для игрока). База: урон = сила + бонус оружия
     * (если есть)
     */
    private int calculateDamage(Character attacker, Character target) {
        int baseDamage;

        if (attacker instanceof Player) {
            baseDamage = ((Player) attacker).calculateDamage(); // Учитывает силу + оружие + баффы

        } else {
            baseDamage = attacker.getStrength();
        }

        // Добавляем случайную вариацию ±20%
        double variation = 0.8 + (random.nextDouble() * 0.4); // 0.8 - 1.2

        return (int) Math.max(1, Math.round(baseDamage * variation));
    }

    /**
     * Применяет специальные эффекты атакующего монстра. Возвращает описание эффекта для отображения.
     */
    private String applyAttackerSpecialEffect(Monster attacker, Character target, int damage) {
        MonsterType type = attacker.getMonsterType();

        switch (type) {
            case VAMPIRE:
                // Вампир восстанавливает здоровье, равное нанесенному урону
                attacker.heal(damage);

                // Также отнимает максимальное здоровье у игрока
                if (target instanceof Player) {
                    Player player = (Player) target;
                    player.decreaseMaxHealth(damage); // Используем новый метод из Character

                    return String.format(
                            "Вампир восстанавливает %d здоровья и крадёт максимум HP! У вас теперь %d/%d HP",
                            damage, player.getHealth(), player.getMaxHealth());
                }
                return "Вампир восстанавливает " + damage + " здоровья!";

            case SNAKE_MAGE:
                // Змей-маг может усыпить игрока
                if (target instanceof Player && random.nextDouble() < 0.3) { // 30% шанс
                    // Усыпление будет обработано в GameController через установку состояния SLEEPING
                    return "Змей-маг усыпляет игрока! Вы пропустите следующий ход.";
                }
                break;

            case OGRE:
                // Огр не имеет специального эффекта при атаке, но будет отдыхать после
                attacker.processAttack(); // Устанавливаем флаг отдыха
                return "Огр атакует и теперь отдыхает!";

            case GHOST:
                // Призрак не имеет специального эффекта при атаке
                break;

            case ZOMBIE:
                // Зомби не имеет специального эффекта при атаке
                break;

            case MIMIC:
                // Мимик не имеет специального эффекта при атаке, но после атаки перестаёт быть предметом
                if (attacker.isInvisible()) {
                    attacker.setInvisible(false);
                    return "Мимик раскрыл себя!";
                }
                break;
        }

        return null;
    }

    /** Применяет эффекты, срабатывающие при получении урона. */
    private void applyOnHitEffects(Monster target, Character attacker, int damageTaken) {
        MonsterType type = target.getMonsterType();

        switch (type) {
            case OGRE:
                break;

            case GHOST:
                // При получении урона призрак становится видимым
                if (target.isInvisible()) {
                    target.setInvisible(false);
                }
                break;

            case MIMIC:
                // При получении урона мимик раскрывается
                if (target.isInvisible()) {
                    target.setInvisible(false);
                }
                break;
        }
    }

    /**
     * Проверяет, может ли монстр атаковать в этом ходу. Используется для огра, который отдыхает после
     * атаки.
     */
    public boolean canMonsterAttack(Monster monster) {
        return monster.canAttack();
    }

    /** Обрабатывает окончание хода для монстра. Используется для снятия флагов отдыха и т.д. */
    public void processMonsterTurnEnd(Monster monster) {
        if (monster.getMonsterType() == MonsterType.OGRE) {
            monster.incrementTurnsSinceLastAttack();
        }
    }

    /**
     * Рассчитывает количество сокровищ, выпадающих с монстра. Зависит от сложности монстра и номера
     * уровня.
     */
    public int calculateTreasureDrop(Monster monster, int levelNumber) {
        MonsterType type = monster.getMonsterType();
        int baseTreasure = type.getTreasureBase();

        // Увеличиваем сокровища с глубиной уровня
        double levelMultiplier = 1.0 + (levelNumber * 0.1); // +10% за уровень
        int treasure = (int) Math.round(baseTreasure * levelMultiplier);

        // Добавляем случайную вариацию ±30%
        double variation = 0.7 + (random.nextDouble() * 0.6); // 0.7 - 1.3
        treasure = (int) Math.round(treasure * variation);

        return Math.max(1, treasure);
    }

    /** Получает тип монстра из цели, если это монстр. */
    private MonsterType getMonsterType(org.example.domain.model.entity.Character character) {
        if (character instanceof Monster) {
            return ((Monster) character).getMonsterType();
        }
        return null;
    }
}
