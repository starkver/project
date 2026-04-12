package org.example.infrastructure.presentation.screen;

import com.googlecode.lanterna.screen.Screen;

public interface MenuScreen {
    void drawMenu(Screen screen) throws Exception;

    void moveUp();

    void moveDown();

    int getSelectedOption();
}
