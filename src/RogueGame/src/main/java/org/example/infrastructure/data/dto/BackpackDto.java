package org.example.infrastructure.data.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO для рюкзака игрока. Хранит списки предметов по типам (максимум 9 каждого типа). Сокровища
 * хранятся как общая стоимость.
 */
public class BackpackDto {
    private List<String> food; // подтипы еды (BREAD, MEAT, APPLE)
    private List<String> elixirs; // подтипы эликсиров
    private List<String> scrolls; // подтипы свитков
    private List<String> weapons; // подтипы оружия
    private int totalTreasureValue; // общая стоимость сокровищ

    public BackpackDto() {
        this.food = new ArrayList<>();
        this.elixirs = new ArrayList<>();
        this.scrolls = new ArrayList<>();
        this.weapons = new ArrayList<>();
        this.totalTreasureValue = 0;
    }

    // Геттеры и сеттеры
    public List<String> getFood() {
        return food;
    }

    public void setFood(List<String> food) {
        this.food = food;
    }

    public List<String> getElixirs() {
        return elixirs;
    }

    public void setElixirs(List<String> elixirs) {
        this.elixirs = elixirs;
    }

    public List<String> getScrolls() {
        return scrolls;
    }

    public void setScrolls(List<String> scrolls) {
        this.scrolls = scrolls;
    }

    public List<String> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<String> weapons) {
        this.weapons = weapons;
    }

    public int getTotalTreasureValue() {
        return totalTreasureValue;
    }

    public void setTotalTreasureValue(int totalTreasureValue) {
        this.totalTreasureValue = totalTreasureValue;
    }
}
