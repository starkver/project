package org.example.infrastructure.presentation.screen;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.util.List;
import org.example.infrastructure.data.repository.StatisticsRepository;

public class ScoresMenuScreen implements MenuScreen {
    private final String[] options = {"Back"};
    private int selected = 0;

    private static final int BACK_OPTION = 0;
    private static final int TABLE_ROW_LIMIT = 10;

    private List<StatisticsRepository.SessionStatDto> topScores;
    private final StatisticsRepository repository;

    public ScoresMenuScreen(StatisticsRepository repository) {
        this.repository = repository;
        this.topScores = repository.getTopScoreboard(TABLE_ROW_LIMIT);
    }

    @Override
    public void drawMenu(Screen screen) throws Exception {
        screen.clear();

        String title = "=== TOP SCORES ===";
        for (int i = 0; i < title.length(); i++) {
            screen.setCharacter(
                    5 + i,
                    0,
                    new TextCharacter(title.charAt(i), TextColor.ANSI.YELLOW, TextColor.ANSI.BLACK));
        }

        // Заголовки таблицы
        String headers =
                String.format(
                        "%-4s %-12s %-8s %-10s %-8s %-8s %-8s %-10s",
                        "№", "Treasures", "Level", "Kills", "Food", "Elixirs", "Scrolls", "Steps");

        for (int j = 0; j < headers.length(); j++) {
            screen.setCharacter(
                    2 + j,
                    2,
                    new TextCharacter(headers.charAt(j), TextColor.ANSI.CYAN, TextColor.ANSI.BLACK));
        }

        // Данные
        int line = 4;
        if (topScores.isEmpty()) {
            String noData = "No scores yet. Complete a game to appear here!";
            for (int j = 0; j < noData.length(); j++) {
                screen.setCharacter(
                        2 + j,
                        line,
                        new TextCharacter(noData.charAt(j), TextColor.ANSI.YELLOW, TextColor.ANSI.BLACK));
            }
            line += 2;
        } else {
            for (int i = 0; i < topScores.size() && i < TABLE_ROW_LIMIT; i++) {
                StatisticsRepository.SessionStatDto stat = topScores.get(i);
                String row =
                        String.format(
                                "%-4d %-12d %-8d %-10d %-8d %-8d %-8d %-10d",
                                i + 1,
                                stat.treasures,
                                stat.level,
                                stat.enemies,
                                stat.food,
                                stat.elixirs,
                                stat.scrolls,
                                stat.moves);

                TextColor.ANSI color = (i == 0) ? TextColor.ANSI.YELLOW : TextColor.ANSI.WHITE;

                for (int j = 0; j < row.length(); j++) {
                    screen.setCharacter(
                            2 + j, line, new TextCharacter(row.charAt(j), color, TextColor.ANSI.BLACK));
                }
                line++;
            }
        }

        // Кнопка Back
        for (int i = 0; i < options.length; i++) {
            char pointer = (i == selected) ? '>' : ' ';
            for (int j = 0; j < options[i].length(); j++) {
                screen.setCharacter(
                        2 + j,
                        line + 2,
                        new TextCharacter(options[i].charAt(j), TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
            }
            screen.setCharacter(
                    0, line + 2, new TextCharacter(pointer, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
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

    public void show(Screen screen) throws Exception {
        // Обновляем данные перед показом
        topScores = repository.getTopScoreboard(TABLE_ROW_LIMIT);

        boolean running = true;
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
                    if (choice == BACK_OPTION) { // Back
                        running = false;
                    }
                    break;
                case Escape:
                    running = false;
                    break;
            }
        }
    }
}
