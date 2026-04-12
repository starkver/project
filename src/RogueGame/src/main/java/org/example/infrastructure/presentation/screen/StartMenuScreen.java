package org.example.infrastructure.presentation.screen;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

public class StartMenuScreen implements MenuScreen {
    private final String[] options = {"Start Game", "Continue", "Scores", "Exit"};
    private int selected = 0;

    @Override
    public void drawMenu(Screen screen) throws Exception {
        screen.clear();

        for (int i = 0; i < options.length; i++) {
            char pointer = (i == selected) ? '>' : ' ';
            for (int j = 0; j < options[i].length(); j++) {
                screen.setCharacter(
                        j + 2,
                        i + 2,
                        new TextCharacter(options[i].charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
            }
            screen.setCharacter(
                    0, i + 2, new TextCharacter(pointer, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
        }
        screen.refresh();
    }

    @Override
    public void moveUp() {
        if (selected > 0) selected--;
    }

    @Override
    public void moveDown() {
        if (selected < options.length - 1) selected++;
    }

    @Override
    public int getSelectedOption() {
        return selected;
    }
}
