package com.rogue.data;

import com.rogue.domain.GameSession;
import com.rogue.domain.Level;
import com.rogue.domain.Position;
import com.rogue.domain.entities.Player;
import com.rogue.domain.items.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = SAVE_DIR + "/game_save.json";
    private static final String LEADERBOARD_FILE = SAVE_DIR + "/leaderboard.json";
    private Gson gson;

    public SaveManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        new File(SAVE_DIR).mkdirs();
    }

    public void saveGame(GameSession session) {
        try {
            GameSaveData saveData = new GameSaveData(session);
            try (FileWriter writer = new FileWriter(SAVE_FILE)) {
                gson.toJson(saveData, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    public GameSession loadGame() {
        try {
            try (FileReader reader = new FileReader(SAVE_FILE)) {
                GameSaveData saveData = gson.fromJson(reader, GameSaveData.class);
                return saveData.restoreSession();
            }
        } catch (IOException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }

    public boolean hasSaveGame() {
        return new File(SAVE_FILE).exists();
    }

    public void saveLeaderboard(GameSession session) {
        try {
            LeaderboardData leaderboard = loadLeaderboardData();
            LeaderboardEntry entry = new LeaderboardEntry(
                    session.getStatistics().totalGoldCollected,
                    session.getCurrentLevelIndex(),
                    session.getStatistics().enemiesDefeated,
                    new Date()
            );
            leaderboard.addEntry(entry);

            try (FileWriter writer = new FileWriter(LEADERBOARD_FILE)) {
                gson.toJson(leaderboard, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save leaderboard: " + e.getMessage());
        }
    }

    private LeaderboardData loadLeaderboardData() {
        File file = new File(LEADERBOARD_FILE);
        if (!file.exists()) {
            return new LeaderboardData();
        }

        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, LeaderboardData.class);
        } catch (IOException e) {
            return new LeaderboardData();
        }
    }

    public Leaderboard loadLeaderboard() {
        LeaderboardData data = loadLeaderboardData();
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setEntries(data.entries);
        return leaderboard;
    }

    // Public inner classes for JSON serialization
    public static class LeaderboardEntry {
        private int gold;
        private int level;
        private int kills;
        private Date date;

        public LeaderboardEntry(int gold, int level, int kills, Date date) {
            this.gold = gold;
            this.level = level;
            this.kills = kills;
            this.date = date;
        }

        public int getGold() { return gold; }
        public int getLevel() { return level; }
        public int getKills() { return kills; }
        public Date getDate() { return date; }
    }

    public static class Leaderboard {
        private List<LeaderboardEntry> entries = new ArrayList<>();

        public List<LeaderboardEntry> getEntries() { return entries; }
        public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }
    }

    private static class GameSaveData {
        private int currentLevel;
        private PlayerData player;
        private List<LevelData> levels;
        private GameSession.GameStatistics statistics;

        public GameSaveData(GameSession session) {
            this.currentLevel = session.getCurrentLevelIndex();
            this.player = new PlayerData(session.getPlayer());
            this.statistics = session.getStatistics();
        }

        public GameSession restoreSession() {
            GameSession session = new GameSession();
            return session;
        }
    }

    private static class PlayerData {
        private int maxHealth;
        private int currentHealth;
        private int dexterity;
        private int strength;
        private int gold;
        private int level;
        private int experience;
        private String weapon;

        public PlayerData(Player player) {
            this.maxHealth = player.getMaxHealth();
            this.currentHealth = player.getCurrentHealth();
            this.dexterity = player.getDexterity();
            this.strength = player.getStrength();
            this.gold = player.getGold();
            this.level = player.getLevel();
            this.experience = player.getExperience();
            this.weapon = player.getWeapon() != null ? player.getWeapon().getName() : null;
        }
    }

    private static class LevelData {
        private int depth;
        private List<String> rooms;
    }

    private static class LeaderboardData {
        private List<LeaderboardEntry> entries = new ArrayList<>();

        public void addEntry(LeaderboardEntry entry) {
            entries.add(entry);
            entries.sort((a, b) -> Integer.compare(b.gold, a.gold));
            if (entries.size() > 10) {
                entries = entries.subList(0, 10);
            }
        }
    }
}