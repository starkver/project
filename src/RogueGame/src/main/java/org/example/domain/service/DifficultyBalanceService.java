package org.example.domain.service;

import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.MonsterType;

/**
 * Сервис для автоматической подстройки сложности игры под уровень игрока.
 *
 * <p>Собирает метрики производительности игрока и вычисляет коэффициенты сложности для генерации
 * следующих уровней.
 *
 * <p>Принцип работы: - Если игрок легко проходит уровни (мало теряет здоровья, быстро убивает
 * монстров), сложность увеличивается. - Если игрок сталкивается с трудностями (часто теряет
 * здоровье, много использует еду, долго убивает монстров), сложность снижается, а полезных
 * предметов становится больше.
 */
public class DifficultyBalanceService {

    // ============================================================
    // Конфигурационные параметры (можно вынести в properties при необходимости)
    // ============================================================

    // Окно отслеживания метрик (количество ходов или уровней)
    private static final int METRICS_WINDOW_LEVELS = 3;

    // Пороговые значения для определения "легко" / "трудно"
    private static final double HEALTH_LOSS_THRESHOLD_EASY =
            0.15; // <15% потери здоровья за уровень - легко
    private static final double HEALTH_LOSS_THRESHOLD_HARD =
            0.40; // >40% потери здоровья за уровень - трудно

    private static final double FIGHT_EFFICIENCY_THRESHOLD_EASY = 0.8; // >80% попаданий - легко
    private static final double FIGHT_EFFICIENCY_THRESHOLD_HARD = 0.5; // <50% попаданий - трудно

    private static final double HEALING_USAGE_THRESHOLD_HARD =
            3.0; // >3 использования еды за уровень - трудно

    private static final int KILLS_PER_LEVEL_THRESHOLD_EASY = 8; // >8 убийств за уровень - легко
    private static final int KILLS_PER_LEVEL_THRESHOLD_HARD = 3; // <3 убийств за уровень - трудно

    // Минимальный и максимальный множители сложности
    private static final double MIN_DIFFICULTY_MULTIPLIER = 0.5;
    private static final double MAX_DIFFICULTY_MULTIPLIER = 1.8;
    private static final double DEFAULT_DIFFICULTY_MULTIPLIER = 1.0;

    // Скорость адаптации (насколько сильно меняется сложность за уровень)
    private static final double ADAPTATION_SPEED = 0.15;

    private final MetricsWindow metricsWindow;
    private double currentDifficultyMultiplier;
    private int currentLevelNumber;

    // Метрики текущего уровня (сбрасываются при переходе)
    private int currentLevelHealthLost;
    private int currentLevelMaxHealth;
    private int currentLevelHitsDealt;
    private int currentLevelHitsTaken;
    private int currentLevelHealingUsed;
    private int currentLevelKills;
    private int currentLevelSteps;

    // Метрики для отслеживания эффективности боя
    private int currentLevelTotalAttacks;
    private int currentLevelSuccessfulAttacks;

    public DifficultyBalanceService() {
        this.metricsWindow = new MetricsWindow(METRICS_WINDOW_LEVELS);
        this.currentDifficultyMultiplier = DEFAULT_DIFFICULTY_MULTIPLIER;
        this.currentLevelNumber = 1;
        resetCurrentLevelMetrics();
    }

    /**
     * Регистрирует полученный игроком урон.
     *
     * @param damage количество полученного урона
     * @param currentHealth текущее здоровье после получения урона
     * @param maxHealth максимальное здоровье
     */
    public void registerDamageTaken(int damage, int currentHealth, int maxHealth) {
        if (damage > 0) {
            currentLevelHealthLost += damage;
            currentLevelMaxHealth = maxHealth;
            currentLevelHitsTaken++;
        }
    }

    /**
     * Регистрирует нанесённый игроком урон.
     *
     * @param damage нанесённый урон
     * @param hitSuccessful попал ли удар
     */
    public void registerDamageDealt(int damage, boolean hitSuccessful) {
        if (damage > 0) {
            currentLevelTotalAttacks++;
            if (hitSuccessful) {
                currentLevelSuccessfulAttacks++;
                currentLevelHitsDealt++;
            }
        }
    }

    /**
     * Регистрирует использование лечебного предмета (еды).
     *
     * @param healAmount количество восстановленного здоровья
     * @param foodType тип съеденной еды
     */
    public void registerHealingUsed(int healAmount, ItemSubType foodType) {
        if (healAmount > 0) {
            currentLevelHealingUsed++;
        }
    }

    /**
     * Регистрирует убийство монстра.
     *
     * @param monsterType тип убитого монстра
     * @param treasureValue стоимость выпавших сокровищ
     */
    public void registerMonsterKilled(MonsterType monsterType, int treasureValue) {
        currentLevelKills++;
    }

    /** Регистрирует шаг игрока (перемещение). */
    public void registerStep() {
        currentLevelSteps++;
    }

    /**
     * Регистрирует начало нового уровня.
     *
     * @param levelNumber номер уровня
     * @return коэффициент сложности для генерации уровня
     */
    public double registerLevelStart(int levelNumber) {
        this.currentLevelNumber = levelNumber;

        // Если это не первый уровень, сохраняем метрики предыдущего
        if (levelNumber > 1) {
            saveCurrentLevelMetrics();
        }

        resetCurrentLevelMetrics();

        // Адаптируем сложность для нового уровня
        adaptDifficulty();

        return currentDifficultyMultiplier;
    }

    /**
     * Получает текущий коэффициент сложности для генерации уровня.
     *
     * @return коэффициент сложности (0.5 - легко, 1.0 - нормально, 1.8 - сложно)
     */
    public double getDifficultyMultiplier() {
        return currentDifficultyMultiplier;
    }

    /**
     * Получает коэффициент количества предметов. При высокой сложности предметов меньше, при низкой -
     * больше.
     *
     * @return коэффициент количества предметов (0.6 - 1.4)
     */
    public double getItemCountMultiplier() {
        // Обратная зависимость: чем сложнее, тем меньше предметов
        // При сложности 1.8 -> предметов 0.6, при сложности 0.5 -> предметов 1.4
        double itemMultiplier = 1.4 - (currentDifficultyMultiplier - 0.5) * 0.8;
        return Math.max(0.6, Math.min(1.4, itemMultiplier));
    }

    /**
     * Получает коэффициент количества монстров. При высокой сложности монстров больше, при низкой -
     * меньше.
     *
     * @return коэффициент количества монстров (0.6 - 1.4)
     */
    public double getMonsterCountMultiplier() {
        // Прямая зависимость: чем сложнее, тем больше монстров
        // При сложности 0.5 -> монстров 0.6, при сложности 1.8 -> монстров 1.4
        double monsterMultiplier = 0.6 + (currentDifficultyMultiplier - 0.5) * 0.8;
        return Math.max(0.6, Math.min(1.4, monsterMultiplier));
    }

    /**
     * Получает коэффициент шанса появления полезных предметов (еда, эликсиры). При трудностях
     * полезных предметов больше.
     *
     * @return коэффициент полезных предметов (0.7 - 1.5)
     */
    public double getUsefulItemChanceMultiplier() {
        // Обратная зависимость: чем сложнее (игроку трудно), тем больше полезных предметов
        // При сложности 1.8 -> полезных предметов 1.5, при сложности 0.5 -> полезных предметов 0.7
        double usefulMultiplier = 0.7 + (currentDifficultyMultiplier - 0.5) * 0.8;
        return Math.max(0.7, Math.min(1.5, usefulMultiplier));
    }

    /**
     * Получает коэффициент сложности монстров (типы). При высокой сложности появляются более опасные
     * монстры.
     *
     * @return коэффициент сложности монстров (0.5 - 1.5)
     */
    public double getMonsterDifficultyMultiplier() {
        // Прямая зависимость
        double monsterDifficulty = 0.5 + (currentDifficultyMultiplier - 0.5) * 1.0;
        return Math.max(0.5, Math.min(1.5, monsterDifficulty));
    }

    /**
     * Получает коэффициент для эликсиров (временных баффов). При трудностях эликсиров больше.
     *
     * @return коэффициент эликсиров (0.7 - 1.5)
     */
    public double getElixirChanceMultiplier() {
        return getUsefulItemChanceMultiplier();
    }

    // ============================================================
    // Приватные методы
    // ============================================================

    /** Сбрасывает метрики текущего уровня. */
    private void resetCurrentLevelMetrics() {
        currentLevelHealthLost = 0;
        currentLevelMaxHealth = 100; // будет обновлено при получении урона
        currentLevelHitsDealt = 0;
        currentLevelHitsTaken = 0;
        currentLevelHealingUsed = 0;
        currentLevelKills = 0;
        currentLevelSteps = 0;
        currentLevelTotalAttacks = 0;
        currentLevelSuccessfulAttacks = 0;
    }

    /** Сохраняет метрики текущего уровня в окно метрик. */
    private void saveCurrentLevelMetrics() {
        LevelMetrics metrics = new LevelMetrics();

        // Процент потери здоровья (от максимального)
        metrics.healthLossPercent =
                currentLevelMaxHealth > 0 ? (double) currentLevelHealthLost / currentLevelMaxHealth : 0.0;

        // Эффективность боя (процент попаданий)
        metrics.fightEfficiency =
                currentLevelTotalAttacks > 0
                        ? (double) currentLevelSuccessfulAttacks / currentLevelTotalAttacks
                        : 1.0; // если не было атак, считаем что всё хорошо

        // Количество использованной еды
        metrics.healingUsed = currentLevelHealingUsed;

        // Количество убийств
        metrics.kills = currentLevelKills;

        // Количество шагов (чем больше шагов на убийство, тем сложнее)
        metrics.stepsPerKill =
                currentLevelKills > 0 ? (double) currentLevelSteps / currentLevelKills : Double.MAX_VALUE;

        metricsWindow.addMetrics(metrics);
    }

    /** Адаптирует сложность на основе накопленных метрик. */
    private void adaptDifficulty() {
        if (metricsWindow.isEmpty()) {
            // Недостаточно данных, оставляем текущую сложность
            return;
        }

        LevelMetrics avgMetrics = metricsWindow.getAverage();

        double difficultyChange = 0.0;

        // 1. Анализ потери здоровья
        difficultyChange = healthAnalyze(avgMetrics, difficultyChange);

        // 2. Анализ эффективности боя
        difficultyChange = fightAnalyze(avgMetrics, difficultyChange);

        // 3. Анализ использования лечения
        difficultyChange = ElixirAnalyze(avgMetrics, difficultyChange);

        // 4. Анализ количества убийств
        difficultyChange = killsAnalyze(avgMetrics, difficultyChange);

        // 5. Анализ шагов на убийство (чем больше шагов, тем сложнее)
        difficultyChange = stepsByKillAnalyze(avgMetrics, difficultyChange);

        // Применяем изменение сложности
        double newMultiplier = currentDifficultyMultiplier + difficultyChange;

        // Ограничиваем допустимыми пределами
        newMultiplier =
                Math.max(MIN_DIFFICULTY_MULTIPLIER, Math.min(MAX_DIFFICULTY_MULTIPLIER, newMultiplier));

        // Плавное изменение: не меняем слишком резко
        if (Math.abs(newMultiplier - currentDifficultyMultiplier) > ADAPTATION_SPEED * 2) {
            newMultiplier =
                    currentDifficultyMultiplier
                            + Math.signum(newMultiplier - currentDifficultyMultiplier) * ADAPTATION_SPEED * 2;
            newMultiplier =
                    Math.max(MIN_DIFFICULTY_MULTIPLIER, Math.min(MAX_DIFFICULTY_MULTIPLIER, newMultiplier));
        }

        currentDifficultyMultiplier = newMultiplier;
    }

    private static double stepsByKillAnalyze(LevelMetrics avgMetrics, double difficultyChange) {
        if (avgMetrics.stepsPerKill > 30 && avgMetrics.stepsPerKill < Double.MAX_VALUE) {
            // Много шагов на убийство - трудно
            difficultyChange -= ADAPTATION_SPEED * 0.3;
        } else if (avgMetrics.stepsPerKill < 10 && avgMetrics.kills > 0) {
            // Мало шагов на убийство - легко
            difficultyChange += ADAPTATION_SPEED * 0.3;
        }
        return difficultyChange;
    }

    private static double killsAnalyze(LevelMetrics avgMetrics, double difficultyChange) {
        if (avgMetrics.kills > KILLS_PER_LEVEL_THRESHOLD_EASY) {
            // Игрок много убивает - легко, увеличиваем сложность
            difficultyChange += ADAPTATION_SPEED * 0.5;
        } else if (avgMetrics.kills < KILLS_PER_LEVEL_THRESHOLD_HARD && avgMetrics.kills > 0) {
            // Игрок мало убивает - трудно, уменьшаем сложность
            difficultyChange -= ADAPTATION_SPEED * 0.5;
        }
        return difficultyChange;
    }

    private static double ElixirAnalyze(LevelMetrics avgMetrics, double difficultyChange) {
        if (avgMetrics.healingUsed > HEALING_USAGE_THRESHOLD_HARD) {
            // Игрок часто лечится - трудно, уменьшаем сложность
            difficultyChange -= ADAPTATION_SPEED * 0.5;
        }
        return difficultyChange;
    }

    private static double fightAnalyze(LevelMetrics avgMetrics, double difficultyChange) {
        if (avgMetrics.fightEfficiency > FIGHT_EFFICIENCY_THRESHOLD_EASY) {
            // Игрок хорошо попадает - увеличиваем сложность
            difficultyChange += ADAPTATION_SPEED * 0.7;
        } else if (avgMetrics.fightEfficiency < FIGHT_EFFICIENCY_THRESHOLD_HARD) {
            // Игрок плохо попадает - уменьшаем сложность
            difficultyChange -= ADAPTATION_SPEED * 0.7;
        }
        return difficultyChange;
    }

    private static double healthAnalyze(LevelMetrics avgMetrics, double difficultyChange) {
        if (avgMetrics.healthLossPercent < HEALTH_LOSS_THRESHOLD_EASY) {
            // Игрок теряет мало здоровья - увеличиваем сложность
            difficultyChange += ADAPTATION_SPEED;
        } else if (avgMetrics.healthLossPercent > HEALTH_LOSS_THRESHOLD_HARD) {
            // Игрок теряет много здоровья - уменьшаем сложность
            difficultyChange -= ADAPTATION_SPEED;
        }
        return difficultyChange;
    }

    /** Сбрасывает всю статистику (при начале новой игры). */
    public void reset() {
        metricsWindow.clear();
        currentDifficultyMultiplier = DEFAULT_DIFFICULTY_MULTIPLIER;
        currentLevelNumber = 1;
        resetCurrentLevelMetrics();
    }

    /** Получает текстовое описание текущего уровня сложности. */
    public String getDifficultyDescription() {
        if (currentDifficultyMultiplier <= 0.7) {
            return "Лёгкий";
        } else if (currentDifficultyMultiplier <= 0.9) {
            return "Средний";
        } else if (currentDifficultyMultiplier <= 1.1) {
            return "Нормальный";
        } else if (currentDifficultyMultiplier <= 1.4) {
            return "Сложный";
        } else {
            return "Очень сложный";
        }
    }

    /** Метрики одного уровня. */
    private static class LevelMetrics {
        double healthLossPercent; // процент потери здоровья
        double fightEfficiency; // процент попаданий
        int healingUsed; // количество использованной еды
        int kills; // количество убитых монстров
        double stepsPerKill; // шагов на одно убийство

        LevelMetrics() {
            this.healthLossPercent = 0.0;
            this.fightEfficiency = 1.0;
            this.healingUsed = 0;
            this.kills = 0;
            this.stepsPerKill = Double.MAX_VALUE;
        }
    }

    /** Окно для хранения метрик последних уровней. */
    private static class MetricsWindow {
        private final LevelMetrics[] metrics;
        private int index;
        private int size;

        MetricsWindow(int capacity) {
            this.metrics = new LevelMetrics[capacity];
            for (int i = 0; i < capacity; i++) {
                metrics[i] = new LevelMetrics();
            }
            this.index = 0;
            this.size = 0;
        }

        void addMetrics(LevelMetrics newMetrics) {
            metrics[index] = newMetrics;
            index = (index + 1) % metrics.length;
            if (size < metrics.length) {
                size++;
            }
        }

        boolean isEmpty() {
            return size == 0;
        }

        void clear() {
            for (int i = 0; i < metrics.length; i++) {
                metrics[i] = new LevelMetrics();
            }
            index = 0;
            size = 0;
        }

        LevelMetrics getAverage() {
            if (size == 0) {
                return new LevelMetrics();
            }

            LevelMetrics avg = new LevelMetrics();
            double sumHealthLoss = 0.0;
            double sumFightEfficiency = 0.0;
            double sumHealingUsed = 0.0;
            double sumKills = 0.0;
            double sumStepsPerKill = 0.0;
            int validStepsCount = 0;

            for (int i = 0; i < size; i++) {
                LevelMetrics m = metrics[i];
                sumHealthLoss += m.healthLossPercent;
                sumFightEfficiency += m.fightEfficiency;
                sumHealingUsed += m.healingUsed;
                sumKills += m.kills;

                if (m.stepsPerKill < Double.MAX_VALUE) {
                    sumStepsPerKill += m.stepsPerKill;
                    validStepsCount++;
                }
            }

            avg.healthLossPercent = sumHealthLoss / size;
            avg.fightEfficiency = sumFightEfficiency / size;
            avg.healingUsed = (int) Math.round(sumHealingUsed / size);
            avg.kills = (int) Math.round(sumKills / size);
            avg.stepsPerKill = validStepsCount > 0 ? sumStepsPerKill / validStepsCount : Double.MAX_VALUE;

            return avg;
        }
    }
}
