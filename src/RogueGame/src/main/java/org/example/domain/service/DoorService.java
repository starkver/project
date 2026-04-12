package org.example.domain.service;

import java.util.List;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.ItemSubType;
import org.example.domain.model.enums.KeyColor;

/** Сервис для работы с дверями и ключами. */
public class DoorService {

    /** Проверяет, может ли игрок открыть дверь указанного цвета. */
    // DoorService.java - исправленный метод canOpenDoor

    public boolean canOpenDoor(Player player, KeyColor doorColor) {
        Backpack backpack = player.getBackpack();
        if (backpack == null) {
            return false;
        }

        // Проверяем наличие ключа в списке ключей
        for (Key key : backpack.getKeys()) {
            if (key.getColor() == doorColor) {
                return true;
            }
        }

        // Проверяем наличие ключа в других списках (на всякий случай)
        ItemSubType keySubType = doorColor.toItemSubType();

        for (Food item : backpack.getFood()) {
            if (item.getSubType() == keySubType) return true;
        }
        for (Elixir item : backpack.getElixirs()) {
            if (item.getSubType() == keySubType) return true;
        }
        for (Scroll item : backpack.getScrolls()) {
            if (item.getSubType() == keySubType) return true;
        }
        for (Weapon item : backpack.getWeapons()) {
            if (item.getSubType() == keySubType) return true;
        }

        return false;
    }

    /** Использовать ключ указанного цвета (удалить из рюкзака). */
    public void useKey(Player player, KeyColor color) {
        Backpack backpack = player.getBackpack();
        if (backpack == null) {
            return;
        }

        ItemSubType keySubType = color.toItemSubType();

        // Сначала проверяем список ключей (добавлено)
        List<Key> keys = backpack.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).getColor() == color) {
                backpack.removeKey(i);
                return;
            }
        }

        // Ищем и удаляем ключ из других списков
        List<Food> foods = backpack.getFood();
        for (int i = 0; i < foods.size(); i++) {
            if (foods.get(i).getSubType() == keySubType) {
                backpack.removeFood(i);
                return;
            }
        }

        List<Elixir> elixirs = backpack.getElixirs();
        for (int i = 0; i < elixirs.size(); i++) {
            if (elixirs.get(i).getSubType() == keySubType) {
                backpack.removeElixir(i);
                return;
            }
        }

        List<Scroll> scrolls = backpack.getScrolls();
        for (int i = 0; i < scrolls.size(); i++) {
            if (scrolls.get(i).getSubType() == keySubType) {
                backpack.removeScroll(i);
                return;
            }
        }

        List<Weapon> weapons = backpack.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            if (weapons.get(i).getSubType() == keySubType) {
                backpack.removeWeapon(i);
                return;
            }
        }
    }

    /** Открыть дверь. */
    public void openDoor(Door door) {
        door.open();
    }
}
