// SelectionState.java
package org.example.infrastructure.controller;

public enum SelectionState {
    NONE, // обычный режим
    SELECT_WEAPON, // выбор оружия (0-9, 0 = снять оружие)
    SELECT_FOOD, // выбор еды (1-9)
    SELECT_ELIXIR, // выбор эликсира (1-9)
    SELECT_SCROLL, // выбор свитка (1-9)
    SELECT_KEY // выбор ключа для использования на двери (1-9)
}
