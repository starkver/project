package org.example.domain.model.entity;

import org.example.domain.model.enums.MonsterType;

/** Класс, представляющий монстра. Хранит тип монстра и его состояние. */
public class Monster extends Character {
    private final MonsterType monsterType;

    // Состояния монстра (для специальных механик)
    private boolean isResting; // для огра (отдых после атаки)
    private boolean isInvisible; // для призрака
    private boolean firstAttackDone; // для вампира (был ли уже первый удар)
    private int turnsSinceLastAttack; // для огра (счётчик ходов после атаки)

    public Monster(int x, int y, MonsterType type) {
        super(
                x,
                y,
                type.getBaseHealth(),
                type.getBaseHealth(),
                type.getBaseAgility(),
                type.getBaseStrength());

        this.monsterType = type;

        // Инициализация состояний
        this.isResting = false;
        this.isInvisible = (type == MonsterType.GHOST || type == MonsterType.MIMIC); // Добавляем мимика
        this.firstAttackDone = false;
        this.turnsSinceLastAttack = 0;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public boolean isResting() {
        return isResting;
    }

    public boolean isInvisible() {
        return isInvisible;
    }

    public boolean isFirstAttackDone() {
        return firstAttackDone;
    }

    public int getTurnsSinceLastAttack() {
        return turnsSinceLastAttack;
    }

    /** Получить дистанцию, с которой монстр начинает преследование. */
    public int getHostilityRange() {
        return monsterType.getHostilityRange();
    }

    /** Установить состояние отдыха (для огра). */
    public void setResting(boolean resting) {
        this.isResting = resting;
        if (resting) {
            this.turnsSinceLastAttack = 0;
        }
    }

    /** Установить невидимость (для призрака). */
    public void setInvisible(boolean invisible) {
        this.isInvisible = invisible;
    }

    /** Отметить, что первый удар по вампиру уже был. */
    public void markFirstAttackDone() {
        this.firstAttackDone = true;
    }

    /** Увеличить счётчик ходов после атаки. */
    public void incrementTurnsSinceLastAttack() {
        this.turnsSinceLastAttack++;
        // После одного хода отдыха огр снова может атаковать
        if (this.turnsSinceLastAttack >= 1 && monsterType == MonsterType.OGRE) {
            this.isResting = false;
        }
    }

    /** Проверить, может ли монстр атаковать в этом ходу. */
    public boolean canAttack() {
        if (monsterType == MonsterType.OGRE && isResting) {
            return false;
        }
        return true;
    }

    /** Проверить, может ли монстр использовать диагональное движение. */
    public boolean canMoveDiagonally() {
        return monsterType == MonsterType.SNAKE_MAGE;
    }

    /** Обработать атаку монстра. */
    public void processAttack() {
        if (monsterType == MonsterType.OGRE) {
            this.isResting = true;
            this.turnsSinceLastAttack = 0;
        }
    }

    /** Обработать получение урона монстром. */
    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);

        // При получении урона призрак становится видимым
        if (monsterType == MonsterType.GHOST) {
            this.isInvisible = false;
        }
    }

    /** Обработать телепортацию (для призрака). */
    public void onTeleport() {
        // После телепортации призрак может снова стать невидимым
        if (monsterType == MonsterType.GHOST && !isInvisible) {
            // Шанс стать невидимым после телепортации
            if (Math.random() < 0.3) {
                this.isInvisible = true;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(monsterType.name());
        if (isInvisible) sb.append(" (невидимый)");
        if (isResting) sb.append(" (отдыхает)");
        sb.append(String.format(" [HP: %d/%d]", getHealth(), getMaxHealth()));
        return sb.toString();
    }
}
