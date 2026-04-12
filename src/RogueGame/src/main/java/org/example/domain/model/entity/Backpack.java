package org.example.domain.model.entity;

import java.util.*;
import org.example.domain.model.enums.ItemType;

/**
 * Рюкзак игрока. Хранит до 9 предметов каждого типа (кроме сокровищ). Сокровища хранятся в одной
 * ячейке с общей стоимостью.
 */
public class Backpack {
    // Максимальное количество предметов каждого типа
    private static final int MAX_ITEMS_PER_TYPE = 9;

    // Хранилища для разных типов предметов
    private final List<Food> food = new ArrayList<>();
    private final List<Elixir> elixirs = new ArrayList<>();
    private final List<Scroll> scrolls = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();

    // Сокровища хранятся как общая стоимость (копятся в одной ячейке)
    private int totalTreasureValue = 0;

    /** Добавляет предмет в рюкзак. */
    public boolean addItem(Item item) {
        if (item == null) return false;

        switch (item.getType()) {
            case FOOD:
                return addFood((Food) item);

            case ELIXIR:
                return addElixir((Elixir) item);

            case SCROLL:
                return addScroll((Scroll) item);

            case WEAPON:
                return addWeapon((Weapon) item);

            case TREASURE:
                // Сокровища всегда добавляются (копятся в одной ячейке)
                totalTreasureValue += ((Treasure) item).getValue();
                return true;

            case KEY:
                return addKey((Key) item);

            default:
                return false;
        }
    }

    private boolean addFood(Food item) {
        if (food.size() >= MAX_ITEMS_PER_TYPE) {
            return false; // Рюкзак полон для еды
        }
        return food.add(item);
    }

    private boolean addElixir(Elixir item) {
        if (elixirs.size() >= MAX_ITEMS_PER_TYPE) {
            return false; // Рюкзак полон для эликсиров
        }
        return elixirs.add(item);
    }

    private boolean addScroll(Scroll item) {
        if (scrolls.size() >= MAX_ITEMS_PER_TYPE) {
            return false; // Рюкзак полон для свитков
        }
        return scrolls.add(item);
    }

    private boolean addWeapon(Weapon item) {
        if (weapons.size() >= MAX_ITEMS_PER_TYPE) {
            return false; // Рюкзак полон для оружия
        }
        return weapons.add(item);
    }

    private boolean addKey(Key item) {
        if (keys.size() >= MAX_ITEMS_PER_TYPE) {
            return false; // Рюкзак полон для ключей
        }
        return keys.add(item);
    }

    /** Удаляет и возвращает еду по индексу. */
    public void removeFood(int index) {
        if (index < 0 || index >= food.size()) {
            return;
        }
        food.remove(index);
    }

    /** Удаляет и возвращает эликсир по индексу. */
    public void removeElixir(int index) {
        if (index < 0 || index >= elixirs.size()) {
            return;
        }
        elixirs.remove(index);
    }

    /** Удаляет и возвращает свиток по индексу. */
    public void removeScroll(int index) {
        if (index < 0 || index >= scrolls.size()) {
            return;
        }
        scrolls.remove(index);
    }

    /** Удаляет и возвращает оружие по индексу. */
    public void removeWeapon(int index) {
        if (index < 0 || index >= weapons.size()) {
            return;
        }
        weapons.remove(index);
    }

    /** Удаляет и возвращает ключ по индексу. */
    public void removeKey(int index) {
        if (index < 0 || index >= keys.size()) {
            return;
        }
        keys.remove(index);
    }

    /** Устанавливает общую стоимость сокровищ (используется при загрузке сохранения). */
    public void setTotalTreasureValue(int value) {
        this.totalTreasureValue = Math.max(0, value);
    }

    /** Возвращает копию списка еды. */
    public List<Food> getFood() {
        return new ArrayList<>(food);
    }

    /** Возвращает копию списка эликсиров. */
    public List<Elixir> getElixirs() {
        return new ArrayList<>(elixirs);
    }

    /** Возвращает копию списка свитков. */
    public List<Scroll> getScrolls() {
        return new ArrayList<>(scrolls);
    }

    /** Возвращает копию списка оружия. */
    public List<Weapon> getWeapons() {
        return new ArrayList<>(weapons);
    }

    /** Возвращает копию списка ключей. */
    public List<Key> getKeys() {
        return new ArrayList<>(keys);
    }

    /** Возвращает общую стоимость всех сокровищ. */
    public int getTotalTreasureValue() {
        return totalTreasureValue;
    }

    public boolean hasFood() {
        return !food.isEmpty();
    }

    public boolean hasElixirs() {
        return !elixirs.isEmpty();
    }

    public boolean hasScrolls() {
        return !scrolls.isEmpty();
    }

    public boolean hasWeapons() {
        return !weapons.isEmpty();
    }

    public boolean hasKeys() {
        return !keys.isEmpty();
    }

    public boolean hasTreasure() {
        return totalTreasureValue > 0;
    }

    /** Проверяет, есть ли место для предмета данного типа. */
    public boolean hasSpaceFor(ItemType type) {
        switch (type) {
            case FOOD:
                return food.size() < MAX_ITEMS_PER_TYPE;
            case ELIXIR:
                return elixirs.size() < MAX_ITEMS_PER_TYPE;
            case SCROLL:
                return scrolls.size() < MAX_ITEMS_PER_TYPE;
            case WEAPON:
                return weapons.size() < MAX_ITEMS_PER_TYPE;
            case KEY:
                return keys.size() < MAX_ITEMS_PER_TYPE;
            case TREASURE:
                return true; // Для сокровищ всегда есть место
            default:
                return false;
        }
    }

    /** Проверяет, пуст ли рюкзак. */
    public boolean isEmpty() {
        return food.isEmpty()
                && elixirs.isEmpty()
                && scrolls.isEmpty()
                && weapons.isEmpty()
                && keys.isEmpty()
                && totalTreasureValue == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Рюкзак:\n");
        sb.append("  Еда: ").append(food.size()).append("/9\n");
        sb.append("  Эликсиры: ").append(elixirs.size()).append("/9\n");
        sb.append("  Свитки: ").append(scrolls.size()).append("/9\n");
        sb.append("  Оружие: ").append(weapons.size()).append("/9\n");
        sb.append("  Ключи: ").append(keys.size()).append("/9\n");
        sb.append("  Сокровища: ").append(totalTreasureValue).append(" монет\n");
        return sb.toString();
    }
}
