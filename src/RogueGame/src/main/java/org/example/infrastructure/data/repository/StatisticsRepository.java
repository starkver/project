package org.example.infrastructure.data.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.example.domain.model.entity.GameStatistics;

/** Репозиторий для работы со статистикой прохождений */
public class StatisticsRepository {
    private static final String DATA_DIR = "data";
    private static final String STATISTICS_FILE = "data/statistics.json";
    private static final String SCOREBOARD_FILE = "data/scoreboard.json";
    private final Gson gson;

    private static final int TABLE_ROW_LIMIT = 10;

    public StatisticsRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию статистики: " + e.getMessage());
        }
    }

    // Работа с текущей статистикой сессии (statistics.json)

    // Сохранить статистику текущей сессии
    public void saveSessionStats(GameStatistics stats) {
        if (stats == null) return;

        try {
            // Создаём DTO (простые поля)
            Map<String, Object> simpleStats = new HashMap<>();
            simpleStats.put("treasures", stats.getTotalTreasureCollected());
            simpleStats.put("level", stats.getLevelReached());
            simpleStats.put("enemies", stats.getEnemiesDefeated());
            simpleStats.put("food", stats.getFoodEaten());
            simpleStats.put("elixirs", stats.getElixirsDrunk());
            simpleStats.put("scrolls", stats.getScrollsRead());
            simpleStats.put("missed", stats.getHitsTaken());
            simpleStats.put("moves", stats.getStepsTaken());

            try (Writer writer = new FileWriter(STATISTICS_FILE)) {
                gson.toJson(simpleStats, writer);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения статистики: " + e.getMessage());
        }
    }

    // Загрузить статистику текущей сессии
    public GameStatistics loadSessionStats() {
        try {
            Path path = Paths.get(STATISTICS_FILE);

            if (!Files.exists(path)) {
                return createEmptyStatistics();
            }

            try (Reader reader = new FileReader(STATISTICS_FILE)) {
                SessionStatDto dto = gson.fromJson(reader, SessionStatDto.class);

                GameStatistics stats = createEmptyStatistics();
                stats.setTotalTreasureCollected((int) dto.treasures);
                stats.setLevelReached((int) dto.level);
                stats.setEnemiesDefeated((int) dto.enemies);
                stats.setFoodEaten((int) dto.food);
                stats.setElixirsDrunk((int) dto.elixirs);
                stats.setScrollsRead((int) dto.scrolls);
                stats.setHitsTaken((int) dto.missed);
                stats.setStepsTaken((int) dto.moves);

                return stats;
            }
        } catch (IOException e) {
            return createEmptyStatistics();
        }
    }

    // Работа с таблицей рекордов (scoreboard.json)

    // Обновить таблицу рекордов (добавить текущую сессию)
    public void updateScoreboard(GameStatistics stats) {
        if (stats == null) return;

        ScoreboardDto scoreboard = loadScoreboard();

        SessionStatDto newStat = new SessionStatDto();
        newStat.treasures = stats.getTotalTreasureCollected();
        newStat.level = stats.getLevelReached();
        newStat.enemies = stats.getEnemiesDefeated();
        newStat.food = stats.getFoodEaten();
        newStat.elixirs = stats.getElixirsDrunk();
        newStat.scrolls = stats.getScrollsRead();
        newStat.missed = stats.getHitsTaken();
        newStat.moves = stats.getStepsTaken();

        scoreboard.sessionStats.add(newStat);

        // Сортировка по сокровищам (по убыванию)
        scoreboard.sessionStats.sort((a, b) -> Long.compare(b.treasures, a.treasures));

        // Оставляем только топ-10
        if (scoreboard.sessionStats.size() > TABLE_ROW_LIMIT) {
            scoreboard.sessionStats =
                    new ArrayList<>(scoreboard.sessionStats.subList(0, TABLE_ROW_LIMIT));
        }

        saveScoreboard(scoreboard);
    }

    // Загрузить таблицу рекордов
    public ScoreboardDto loadScoreboard() {
        try {
            Path path = Paths.get(SCOREBOARD_FILE);

            if (!Files.exists(path)) {
                return new ScoreboardDto();
            }

            try (Reader reader = new FileReader(SCOREBOARD_FILE)) {
                return gson.fromJson(reader, ScoreboardDto.class);
            }
        } catch (IOException e) {
            return new ScoreboardDto();
        }
    }

    // Сохранить таблицу рекордов
    private void saveScoreboard(ScoreboardDto scoreboard) {
        try {
            try (Writer writer = new FileWriter(SCOREBOARD_FILE)) {
                gson.toJson(scoreboard, writer);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения таблицы рекордов: " + e.getMessage());
        }
    }

    // Получить топ-N записей из таблицы рекордов
    public List<SessionStatDto> getTopScoreboard(int limit) {
        ScoreboardDto scoreboard = loadScoreboard();
        return scoreboard.sessionStats.stream().limit(limit).toList();
    }

    // Утилиты
    private GameStatistics createEmptyStatistics() {
        return new GameStatistics(System.currentTimeMillis());
    }

    // Внутренние DTO для формата

    // DTO для statistics.json (простая структура)
    public static class SessionStatDto {
        public long treasures = 0;
        public long level = 0;
        public long enemies = 0;
        public long food = 0;
        public long elixirs = 0;
        public long scrolls = 0;
        public long missed = 0;
        public long moves = 0;
    }

    // DTO для таблицы рекордов
    public static class ScoreboardDto {
        public List<SessionStatDto> sessionStats = new ArrayList<>();
    }
}
