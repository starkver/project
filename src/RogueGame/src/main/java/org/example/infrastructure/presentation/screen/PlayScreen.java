package org.example.infrastructure.presentation.screen;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import java.util.List;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.KeyColor;
import org.example.domain.service.VisibilityService;
import org.example.infrastructure.controller.SelectionState;
import org.example.infrastructure.presentation.ui.MapRenderer;

public class PlayScreen {

    private final MapRenderer mapRenderer = new MapRenderer();

    public void draw(
            Screen screen,
            Level level,
            Player player,
            Backpack backpack,
            List<String> notifications,
            SelectionState selectionState,
            VisibilityService visibilityService)
            throws Exception {

        screen.clear();

        mapRenderer.draw(screen, level, visibilityService);

        int levelNumber = level.getLevelNumber();

        drawStatus(screen, player, backpack, levelNumber);

        drawInventory(screen, backpack, selectionState);

        drawNotifications(screen, notifications);

        drawControlsHelp(screen);

        screen.refresh();
    }

    private static void drawStatus(Screen screen, Player player, Backpack backpack, int levelNumber) {
        String equippedWeaponName;
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon != null) {
            equippedWeaponName = currentWeapon.getName();
        } else {
            equippedWeaponName = "Руки";
        }

        // Эффективная сила и ловкость с учётом баффов
        int effectiveStrength = player.getEffectiveStrength();
        int effectiveAgility = player.getEffectiveAgility();

        String[] status = {
                "═════ СТАТУС ═════",
                "Уровень: " + levelNumber + "/21",
                "Здоровье: " + player.getHealth() + "/" + player.getMaxHealth(),
                "Сила: " + player.getStrength() + " (" + effectiveStrength + ")",
                "Ловкость: " + player.getAgility() + " (" + effectiveAgility + ")",
                "Сокровища: " + backpack.getTotalTreasureValue(),
                "Оружие: " + equippedWeaponName,
                "Ходов: " + player.getTurnsLived(),
                "Ключи: " + backpack.getKeys().size() + " шт",
                ""
        };

        // Показываем активные эффекты
        List<TemporaryEffect> activeEffects = player.getActiveEffects();
        if (!activeEffects.isEmpty()) {
            status[status.length - 1] = "═════ ЭФФЕКТЫ ═════";
        }

        for (int i = 0; i < status.length; i++) {
            drawString(screen, 0, i, status[i], TextColor.ANSI.WHITE);
        }

        // Отображаем активные эффекты
        int effectLine = status.length;
        if (!activeEffects.isEmpty()) {
            for (int i = 0; i < Math.min(activeEffects.size(), 5); i++) {
                TemporaryEffect effect = activeEffects.get(i);
                int turnsLeft = effect.durationTurns() - (player.getTurnsLived() - effect.appliedAtTurn());
                String effectText =
                        String.format(
                                "  %s +%d (%d ходов)", getStatName(effect.stat()), effect.bonusValue(), turnsLeft);
                drawString(screen, 2, effectLine + i, effectText, TextColor.ANSI.MAGENTA);
            }
        }
    }

    private static void drawInventory(
            Screen screen, Backpack backpack, SelectionState selectionState) {

        int invLeft = 121;
        int invTop = 0;

        drawTitle(screen, selectionState, invLeft, invTop);

        switch (selectionState) {
            case SELECT_WEAPON -> drawWeapon(screen, backpack, invLeft, invTop);

            case SELECT_FOOD -> drawFood(screen, backpack, invLeft, invTop);

            case SELECT_ELIXIR -> drawElixir(screen, backpack, invLeft, invTop);

            case SELECT_SCROLL -> drawScroll(screen, backpack, invLeft, invTop);

            case SELECT_KEY -> drawKey(screen, backpack, invLeft, invTop);

            default -> drawInventoryInfo(screen, backpack, invLeft, invTop);
        }

        int currentRow = invTop + 18; // чуть ниже инвентаря

        drawKeyInfo(screen, backpack, invLeft, currentRow);
    }

    private static void drawKeyInfo(Screen screen, Backpack backpack, int invLeft, int currentRow) {
        drawString(screen, invLeft, currentRow, "═════ КЛЮЧИ ═════", TextColor.ANSI.YELLOW);
        currentRow++;

        List<Key> keys = backpack.getKeys();
        int screenHeight = screen.getTerminalSize().getRows();
        int maxKeysToShow =
                Math.min(
                        keys.size(),
                        screenHeight - currentRow - 8); // оставляем место для уведомлений и управления

        if (!keys.isEmpty()) {
            for (int i = 0; i < maxKeysToShow; i++) {
                Key key = keys.get(i);
                drawString(
                        screen, invLeft + 2, currentRow + i, key.toString(), getKeyColor(key.getColor()));
            }
            if (keys.size() > maxKeysToShow) {
                drawString(
                        screen,
                        invLeft + 2,
                        currentRow + maxKeysToShow,
                        "... и еще " + (keys.size() - maxKeysToShow) + " ключей",
                        TextColor.ANSI.YELLOW);
            }
        } else {
            drawString(screen, invLeft + 2, currentRow, "Нет ключей", TextColor.ANSI.WHITE);
        }
    }

    private static void drawInventoryInfo(Screen screen, Backpack backpack, int invLeft, int invTop) {
        // Показываем краткую информацию об инвентаре
        drawString(
                screen,
                invLeft,
                invTop + 2,
                "Еда: " + backpack.getFood().size() + "/9",
                TextColor.ANSI.WHITE);
        drawString(
                screen,
                invLeft,
                invTop + 3,
                "Эликсиры: " + backpack.getElixirs().size() + "/9",
                TextColor.ANSI.WHITE);
        drawString(
                screen,
                invLeft,
                invTop + 4,
                "Свитки: " + backpack.getScrolls().size() + "/9",
                TextColor.ANSI.WHITE);
        drawString(
                screen,
                invLeft,
                invTop + 5,
                "Оружие: " + backpack.getWeapons().size() + "/9",
                TextColor.ANSI.WHITE);
        drawString(
                screen,
                invLeft,
                invTop + 6,
                "Ключи: " + backpack.getKeys().size() + "/9",
                TextColor.ANSI.CYAN);

        drawString(screen, invLeft, invTop + 8, "───────────────", TextColor.ANSI.WHITE);
        drawString(screen, invLeft, invTop + 9, "H - Оружие", TextColor.ANSI.CYAN);
        drawString(screen, invLeft, invTop + 10, "J - Еда", TextColor.ANSI.GREEN);
        drawString(screen, invLeft, invTop + 11, "K - Эликсиры", TextColor.ANSI.MAGENTA);
        drawString(screen, invLeft, invTop + 12, "E - Свитки", TextColor.ANSI.BLUE);
        drawString(screen, invLeft, invTop + 13, "U - Ключи", TextColor.ANSI.YELLOW);
        drawString(screen, invLeft, invTop + 15, "Нажмите кнопку", TextColor.ANSI.WHITE);
        drawString(screen, invLeft, invTop + 16, "повторно для выхода", TextColor.ANSI.WHITE);
    }

    private static void drawKey(Screen screen, Backpack backpack, int invLeft, int invTop) {
        List<Key> keys = backpack.getKeys();
        for (int idx = 0; idx < 9; idx++) {
            String text;
            TextColor.ANSI color;
            if (idx < keys.size()) {
                Key key = keys.get(idx);
                text = (idx + 1) + ". " + key.toString();
                color = getKeyColor(key.getColor());
            } else {
                text = (idx + 1) + ". ---";
                color = TextColor.ANSI.WHITE;
            }
            drawString(screen, invLeft, invTop + 2 + idx, text, color);
        }
        drawString(
                screen,
                invLeft,
                invTop + 12,
                "0. Использовать ключ на соседней двери",
                TextColor.ANSI.YELLOW);
    }

    private static void drawScroll(Screen screen, Backpack backpack, int invLeft, int invTop) {
        List<Scroll> scrolls = backpack.getScrolls();
        for (int idx = 0; idx < 9; idx++) {
            String text =
                    (idx < scrolls.size())
                            ? (idx + 1) + ". " + scrolls.get(idx).toString()
                            : (idx + 1) + ". ---";
            drawString(screen, invLeft, invTop + 2 + idx, text, TextColor.ANSI.WHITE);
        }
    }

    private static void drawElixir(Screen screen, Backpack backpack, int invLeft, int invTop) {
        List<Elixir> elixirs = backpack.getElixirs();
        for (int idx = 0; idx < 9; idx++) {
            String text =
                    (idx < elixirs.size())
                            ? (idx + 1) + ". " + elixirs.get(idx).toString()
                            : (idx + 1) + ". ---";
            drawString(screen, invLeft, invTop + 2 + idx, text, TextColor.ANSI.WHITE);
        }
    }

    private static void drawFood(Screen screen, Backpack backpack, int invLeft, int invTop) {
        List<Food> foods = backpack.getFood();
        for (int idx = 0; idx < 9; idx++) {
            String text =
                    (idx < foods.size())
                            ? (idx + 1)
                            + ". "
                            + foods.get(idx).getName()
                            + " (+"
                            + foods.get(idx).getHealAmount()
                            + " HP)"
                            : (idx + 1) + ". ---";
            drawString(screen, invLeft, invTop + 2 + idx, text, TextColor.ANSI.WHITE);
        }
    }

    private static void drawWeapon(Screen screen, Backpack backpack, int invLeft, int invTop) {
        List<Weapon> weapons = backpack.getWeapons();
        for (int idx = 0; idx < 9; idx++) {
            String text =
                    (idx < weapons.size())
                            ? (idx + 1)
                            + ". "
                            + weapons.get(idx).getName()
                            + " (урон +"
                            + weapons.get(idx).getDamageBonus()
                            + ")"
                            : (idx + 1) + ". ---";
            drawString(screen, invLeft, invTop + 2 + idx, text, TextColor.ANSI.WHITE);
        }
        drawString(screen, invLeft, invTop + 12, "0. Снять оружие", TextColor.ANSI.RED);
    }

    private static void drawTitle(
            Screen screen, SelectionState selectionState, int invLeft, int invTop) {
        String title =
                switch (selectionState) {
                    case SELECT_WEAPON -> "══ ОРУЖИЕ (0-9, 0 = снять) ══";
                    case SELECT_FOOD -> "══ ЕДА (1-9) ══";
                    case SELECT_ELIXIR -> "══ ЭЛИКСИРЫ (1-9) ══";
                    case SELECT_SCROLL -> "══ СВИТКИ (1-9) ══";
                    case SELECT_KEY -> "══ КЛЮЧИ (1-9, 0 = применить к двери) ══";
                    default -> "═════ ИНВЕНТАРЬ ═════";
                };

        drawString(screen, invLeft, invTop, title, TextColor.ANSI.YELLOW);
    }

    private static void drawNotifications(Screen screen, List<String> notifications) {
        int notifTop = 32;
        int notifLeft = 0;
        int maxWidth = 120;

        drawString(
                screen,
                notifLeft,
                notifTop - 1,
                "═══════════════ СООБЩЕНИЯ ═══════════════",
                TextColor.ANSI.YELLOW);

        for (int i = 0; i < notifications.size() && i < 5; i++) {
            String msg = notifications.get(i);
            if (msg.length() > maxWidth) {
                msg = msg.substring(0, maxWidth - 3) + "...";
            }
            drawString(screen, notifLeft, notifTop + i, msg, TextColor.ANSI.YELLOW);
        }
    }

    private static void drawControlsHelp(Screen screen) {
        int helpLeft = 0;
        int helpTop = 38;

        String[] controls = {
                "═══════════════ УПРАВЛЕНИЕ ═══════════════",
                "WASD / Стрелки - движение",
                "I - закрыть инвентарь",
                "H - оружие | J - еда | K - эликсиры | E - свитки | U - ключи",
                "ESC - выход в меню",
                "f - отключить туман войны"
        };

        for (int i = 0; i < controls.length; i++) {
            drawString(screen, helpLeft, helpTop + i, controls[i], TextColor.ANSI.WHITE);
        }
    }

    private static void drawString(Screen screen, int x, int y, String text, TextColor.ANSI color) {
        for (int i = 0; i < text.length(); i++) {
            if (x + i >= 0
                    && x + i < screen.getTerminalSize().getColumns()
                    && y >= 0
                    && y < screen.getTerminalSize().getRows()) {
                screen.setCharacter(
                        x + i,
                        y,
                        new com.googlecode.lanterna.TextCharacter(text.charAt(i), color, TextColor.ANSI.BLACK));
            }
        }
    }

    private static String getStatName(org.example.domain.model.enums.StatType stat) {
        return switch (stat) {
            case STRENGTH -> "Сила";
            case AGILITY -> "Ловкость";
            case MAX_HEALTH -> "Макс. HP";
        };
    }

    private static TextColor.ANSI getKeyColor(KeyColor color) {
        return switch (color) {
            case RED -> TextColor.ANSI.RED;
            case BLUE -> TextColor.ANSI.BLUE;
            case YELLOW -> TextColor.ANSI.YELLOW;
            case GREEN -> TextColor.ANSI.GREEN;
            case PURPLE -> TextColor.ANSI.MAGENTA;
        };
    }
}
