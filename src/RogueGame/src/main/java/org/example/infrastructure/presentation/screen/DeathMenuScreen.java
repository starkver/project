package org.example.infrastructure.presentation.screen;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import org.example.domain.model.entity.GameSession;
import org.example.infrastructure.data.repository.StatisticsRepository;

public class DeathMenuScreen implements MenuScreen {
    private final String[] options = {"Restart", "Statistics", "Exit"};
    private int selected = 0;

    private static final int RESTART_GAME_OPTION = 0;
    private static final int SCORES_OPTION = 1;
    private static final int EXIT_OPTION = 2;

    private final StatisticsRepository statisticsRepository;

    public DeathMenuScreen(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void drawMenu(Screen screen) throws Exception {
        screen.clear();

        String deathMessage = "YOU DIED";
        for (int i = 0; i < deathMessage.length(); i++) {
            screen.setCharacter(
                    5 + i,
                    0,
                    new TextCharacter(deathMessage.charAt(i), TextColor.ANSI.RED, TextColor.ANSI.BLACK));
        }

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
        if (selected > 0) {
            selected--;
        }
    }

    @Override
    public void moveDown() {
        if (selected < options.length - 1) {
            selected++;
        }
    }

    @Override
    public int getSelectedOption() {
        return selected;
    }

    public void show(Screen screen, GameSession gameSession) throws Exception {
        boolean running = true;
        boolean restart = false;

        while (running) {
            drawMenu(screen);

            KeyStroke key = screen.readInput();
            if (key == null) continue;

            switch (key.getKeyType()) {
                case ArrowUp:
                    moveUp();
                    break;
                case ArrowDown:
                    moveDown();
                    break;
                case Enter:
                    int choice = getSelectedOption();
                    switch (choice) {
                        case RESTART_GAME_OPTION: // Restart
                            restart = true;
                            running = false;
                            break;
                        case SCORES_OPTION: // показываем таблицу рекордов
                            ScoresMenuScreen scoresMenu = new ScoresMenuScreen(statisticsRepository);
                            scoresMenu.show(screen);
                            break;
                        case EXIT_OPTION: // Выход
                            System.exit(0);
                            break;
                    }
                    break;
                case Escape:
                    running = false;
                    break;
            }
        }
    }
}
