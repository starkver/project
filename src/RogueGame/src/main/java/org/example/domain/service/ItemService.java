package org.example.domain.service;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.*;

/**
 * Сервис отвечающий за работу с предметами: - использование предметов (еда, эликсиры, свитки,
 * ключи) - экипировка оружия - управление рюкзаком - применение эффектов
 */
public class ItemService {

    private final StatisticsService statisticsService;
    private DifficultyBalanceService difficultyBalanceService;

    /** Конструктор с внедрением DifficultyBalanceService. */
    public ItemService(
            StatisticsService statisticsService, DifficultyBalanceService difficultyBalanceService) {
        this.statisticsService = statisticsService;
        this.difficultyBalanceService = difficultyBalanceService;
    }

    /** Устанавливает сервис балансировки сложности. */
    public void setDifficultyBalanceService(DifficultyBalanceService difficultyBalanceService) {
        this.difficultyBalanceService = difficultyBalanceService;
    }

    /**
     * Добавляет предмет в рюкзак игрока. Проверяет лимиты: 9 предметов каждого типа, сокровища в
     * одной ячейке.
     */
    public boolean addToBackpack(Player player, Item item, long sessionId) {
        Backpack backpack = player.getBackpack();

        if (backpack == null) {
            backpack = new Backpack();
            player.setBackpack(backpack);
        }

        if (!backpack.hasSpaceFor(item.getType())) {
            return false;
        }

        boolean added = backpack.addItem(item);

        if (added) {
            statisticsService.updateItemPickup(sessionId, item.getType());
        }

        return added;
    }

    /** Добавляет предмет на пол (выбрасывает из рюкзака). */
    public void dropToFloor(Level level, Position position, Item item) {
        if (item == null) return;

        Tile tile = level.getTileMap().getTile(position);
        if (tile == null || !tile.isWalkable() || tile.hasItem()) {
            return; // Нельзя бросить сюда
        }

        level.addItem(item, position.x(), position.y());
    }

    /** Использование еды (мгновенное лечение). */
    public ItemUseResult useFood(Player player, int index, Level level, long sessionId) {
        Backpack backpack = player.getBackpack();
        if (backpack == null || !backpack.hasFood()) {
            return new ItemUseResult(false, "Нет еды в рюкзаке", null);
        }

        List<Food> foodItems = backpack.getFood();
        if (index < 0 || index >= foodItems.size()) {
            return new ItemUseResult(false, "Неверный индекс предмета", null);
        }

        Food food = foodItems.get(index);

        // Проверяем, есть ли смысл использовать
        if (!food.isUseful(player)) {
            return new ItemUseResult(false, "У вас уже полное здоровье", food);
        }

        // Запоминаем здоровье до лечения
        int oldHealth = player.getHealth();

        // Применяем еду
        int healedAmount = food.applyTo(player);

        // Удаляем использованную еду из рюкзака
        backpack.removeFood(index);

        // Регистрируем использование лечения в системе балансировки
        if (difficultyBalanceService != null) {
            difficultyBalanceService.registerHealingUsed(healedAmount, food.getSubType());
        }

        // Обновляем статистику
        statisticsService.updateFoodEaten(sessionId, food.getSubType(), healedAmount);

        String message =
                String.format(
                        "Съедено: %s. Восстановлено %d HP. Текущее HP: %d/%d",
                        food.getName(), healedAmount, player.getHealth(), player.getMaxHealth());

        return new ItemUseResult(true, message, food);
    }

    /** Использование эликсира (временный бафф). */
    public ItemUseResult useElixir(
            Player player, int index, int currentTurn, Level level, long sessionId) {
        Backpack backpack = player.getBackpack();
        if (backpack == null || !backpack.hasElixirs()) {
            return new ItemUseResult(false, "Нет эликсиров в рюкзаке", null);
        }

        List<Elixir> elixirItems = backpack.getElixirs();
        if (index < 0 || index >= elixirItems.size()) {
            return new ItemUseResult(false, "Неверный индекс предмета", null);
        }

        Elixir elixir = elixirItems.get(index);
        TemporaryEffect effect = elixir.applyTo(player, currentTurn);
        backpack.removeElixir(index);

        statisticsService.updateElixirDrunk(sessionId, elixir.getSubType());

        StatType stat = effect.stat();
        String statName =
                switch (stat) {
                    case STRENGTH -> "Силы";
                    case AGILITY -> "Ловкости";
                    case MAX_HEALTH -> "Макс. здоровья";
                };

        String message =
                String.format(
                        "Выпит эликсир: %s. %s +%d на %d ходов.",
                        elixir.getName(), statName, effect.bonusValue(), effect.durationTurns());

        return new ItemUseResult(true, message, elixir);
    }

    /** Использование свитка (постоянный бафф). */
    public ItemUseResult useScroll(Player player, int index, Level level, long sessionId) {
        Backpack backpack = player.getBackpack();
        if (backpack == null || !backpack.hasScrolls()) {
            return new ItemUseResult(false, "Нет свитков в рюкзаке", null);
        }

        List<Scroll> scrollItems = backpack.getScrolls();
        if (index < 0 || index >= scrollItems.size()) {
            return new ItemUseResult(false, "Неверный индекс предмета", null);
        }

        Scroll scroll = scrollItems.get(index);

        int oldStrength = player.getStrength();
        int oldAgility = player.getAgility();
        int oldMaxHealth = player.getMaxHealth();

        scroll.applyTo(player);
        backpack.removeScroll(index);

        statisticsService.updateScrollRead(sessionId, scroll.getSubType());

        String message = formatScrollResult(scroll, player, oldStrength, oldAgility, oldMaxHealth);

        return new ItemUseResult(true, message, scroll);
    }

    /** Экипировка оружия из рюкзака. */
    public EquipResult equipWeapon(Player player, int index, Level level, long sessionId) {
        Backpack backpack = player.getBackpack();

        // Случай 1: Снять оружие (index = -1)
        if (index < 0) {
            if (!player.isWeaponEquipped()) {
                return new EquipResult(false, "Нет экипированного оружия", null, null);
            }

            Weapon oldWeapon = player.unequipWeapon();
            boolean added = backpack.addItem(oldWeapon);

            if (!added) {
                Position dropPos = findDropPosition(level, player.getX(), player.getY());
                dropToFloor(level, dropPos, oldWeapon);
                return new EquipResult(
                        true, "Оружие снято, но рюкзак полон - оно выпало на пол", null, oldWeapon);
            }

            return new EquipResult(true, "Оружие снято и убрано в рюкзак", null, oldWeapon);
        }

        // Случай 2: Экипировать новое оружие
        if (backpack == null || !backpack.hasWeapons()) {
            return new EquipResult(false, "Нет оружия в рюкзаке", null, null);
        }

        List<Weapon> weaponItems = backpack.getWeapons();
        if (index >= weaponItems.size()) {
            return new EquipResult(false, "Неверный индекс предмета", null, null);
        }

        Weapon newWeapon = weaponItems.get(index);
        Weapon oldWeapon = player.getCurrentWeapon();

        backpack.removeWeapon(index);
        player.equipWeapon(newWeapon);

        if (oldWeapon != null) {
            boolean added = backpack.addItem(oldWeapon);
            if (!added) {
                Position dropPos = findDropPosition(level, player.getX(), player.getY());
                dropToFloor(level, dropPos, oldWeapon);

                String message =
                        String.format(
                                "Экипировано: %s. Старое оружие выпало на пол (рюкзак полон)", newWeapon.getName());
                return new EquipResult(true, message, newWeapon, oldWeapon);
            }
        }

        statisticsService.updateWeaponEquipped(sessionId, newWeapon.getSubType());

        String message =
                String.format(
                        "Экипировано: %s (сила +%d)", newWeapon.getName(), newWeapon.getDamageBonus());

        return new EquipResult(true, message, newWeapon, oldWeapon);
    }

    /** Находит позицию для выбрасывания предмета рядом с игроком. */
    private Position findDropPosition(Level level, int playerX, int playerY) {
        TileMap tileMap = level.getTileMap();

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] dir : directions) {
            int nx = playerX + dir[0];
            int ny = playerY + dir[1];

            if (tileMap.isWithinBounds(nx, ny)) {
                Tile tile = tileMap.getTile(nx, ny);
                if (tile.isWalkable() && !tile.hasItem()) {
                    return new Position(nx, ny);
                }
            }
        }

        return new Position(playerX, playerY);
    }

    /** Форматирует результат применения свитка. */
    private String formatScrollResult(
            Scroll scroll, Player player, int oldStrength, int oldAgility, int oldMaxHealth) {
        StringBuilder sb = new StringBuilder();
        sb.append("Прочитан свиток: ").append(scroll.getName()).append(". ");

        ItemSubType subType = scroll.getSubType();
        switch (subType) {
            case SCROLL_STRENGTH:
                sb.append(String.format("Сила: %d -> %d", oldStrength, player.getStrength()));
                break;
            case SCROLL_AGILITY:
                sb.append(String.format("Ловкость: %d -> %d", oldAgility, player.getAgility()));
                break;
            case SCROLL_MAX_HEALTH:
                sb.append(String.format("Макс. HP: %d -> %d", oldMaxHealth, player.getMaxHealth()));
                sb.append(String.format(", Текущее HP: %d", player.getHealth()));
                break;
            default:
                // Ничего не добавляем
        }

        return sb.toString();
    }

    /** Получает сервис балансировки сложности. */
    public DifficultyBalanceService getDifficultyBalanceService() {
        return difficultyBalanceService;
    }
}
