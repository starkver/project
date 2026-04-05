package com.rogue.domain;

import com.rogue.domain.entities.*;
import com.rogue.domain.items.*;
import com.rogue.domain.combat.CombatResolver;
import com.rogue.domain.generation.DungeonGenerator;
import java.util.*;

public class GameSession {
    private Player player;
    private List<Level> levels;
    private int currentLevelIndex;
    private boolean isGameOver;
    private boolean isVictory;
    private GameStatistics statistics;
    private DungeonGenerator generator;
    private Random random;

    public GameSession() {
        this.player = new Player();
        this.levels = new ArrayList<>();
        this.currentLevelIndex = 0;
        this.isGameOver = false;
        this.isVictory = false;
        this.statistics = new GameStatistics();
        this.generator = new DungeonGenerator();
        this.random = new Random();

        // Generate first level
        generateCurrentLevel();

        // Set player start position
        Level currentLevel = getCurrentLevel();
        Room startRoom = currentLevel.getStartRoom();
        Position startPos = startRoom.getRandomFloorPosition();
        player.setPosition(startPos);
    }

    private void generateCurrentLevel() {
        while (levels.size() <= currentLevelIndex) {
            levels.add(generator.generateLevel(levels.size() + 1));
        }
    }

    public Level getCurrentLevel() {
        generateCurrentLevel();
        return levels.get(currentLevelIndex);
    }

    public boolean movePlayer(Direction direction) {
        if (isGameOver) return false;

        Position newPos = player.getPosition().add(direction);
        Level level = getCurrentLevel();

        // Check if move is valid
        if (!level.isWalkable(newPos)) {
            // Check for enemy at position
            Enemy enemy = level.getEnemyAt(newPos);
            if (enemy != null) {
                combatWithEnemy(enemy);
                return true;
            }
            return false;
        }

        // Pick up item if present
        Item item = level.getItemAt(newPos);
        if (item != null) {
            if (player.pickUpItem(item)) {
                level.removeItem(newPos);
                statistics.itemsCollected++;
            }
        }

        // Move player
        player.setPosition(newPos);
        statistics.cellsMoved++;

        // Check for exit
        if (level.isExit(newPos)) {
            nextLevel();
        }

        // Enemy turn
        enemyTurn();

        // Update player buffs
        player.updateBuffs();

        // Check for game over
        if (!player.isAlive()) {
            isGameOver = true;
        }

        return true;
    }

    private void combatWithEnemy(Enemy enemy) {
        Level level = getCurrentLevel();

        // Player attacks first
        CombatResolver.CombatResult result = CombatResolver.resolveAttack(player, enemy);
        statistics.totalAttacks++;

        if (result.isMiss()) {
            statistics.missedAttacks++;
        } else if (result.getDamage() > 0) {
            statistics.totalDamageDealt += result.getDamage();
        }

        if (!enemy.isAlive()) {
            // Enemy dies
            level.removeEnemy(enemy);
            int treasure = enemy.getTreasureValue();
            player.addGold(treasure);
            statistics.totalGoldCollected += treasure;
            statistics.enemiesDefeated++;
            player.gainExperience(10 + random.nextInt(20));
            return;
        }

        // Enemy counterattacks
        result = CombatResolver.resolveAttack(enemy, player);
        if (!result.isMiss() && result.getDamage() > 0) {
            statistics.totalDamageTaken += result.getDamage();
        }

        if (!player.isAlive()) {
            isGameOver = true;
        }
    }

    private void enemyTurn() {
        Level level = getCurrentLevel();
        List<Enemy> enemies = new ArrayList<>(level.getEnemies().values());

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            if (enemy.isHostileTo(player)) {
                moveEnemyTowardPlayer(enemy);
            } else {
                randomMoveEnemy(enemy);
            }
        }
    }

    private void moveEnemyTowardPlayer(Enemy enemy) {
        Level level = getCurrentLevel();
        Position playerPos = player.getPosition();
        Position enemyPos = enemy.getPosition();

        // Simple pathfinding: move in direction that reduces distance
        int dx = Integer.compare(playerPos.x, enemyPos.x);
        int dy = Integer.compare(playerPos.y, enemyPos.y);

        // Try to move horizontally first
        Position newPos = new Position(enemyPos.x + dx, enemyPos.y);
        if (level.isWalkable(newPos) && level.getEnemyAt(newPos) == null) {
            level.removeEnemy(enemy);
            enemy.setPosition(newPos);
            level.addEnemy(enemy);
            return;
        }

        // Then vertically
        newPos = new Position(enemyPos.x, enemyPos.y + dy);
        if (level.isWalkable(newPos) && level.getEnemyAt(newPos) == null) {
            level.removeEnemy(enemy);
            enemy.setPosition(newPos);
            level.addEnemy(enemy);
        }
    }

    private void randomMoveEnemy(Enemy enemy) {
        Level level = getCurrentLevel();
        List<Direction> directions = Arrays.asList(Direction.values());
        Collections.shuffle(directions);

        for (Direction dir : directions) {
            Position newPos = enemy.getPosition().add(dir);
            if (level.isWalkable(newPos) && level.getEnemyAt(newPos) == null) {
                level.removeEnemy(enemy);
                enemy.setPosition(newPos);
                level.addEnemy(enemy);
                break;
            }
        }
    }

    private void nextLevel() {
        currentLevelIndex++;

        if (currentLevelIndex >= 21) {
            // Victory!
            isVictory = true;
            isGameOver = true;
            return;
        }

        // Generate next level if needed
        generateCurrentLevel();

        // Place player in start room of new level
        Level newLevel = getCurrentLevel();
        Room startRoom = newLevel.getStartRoom();
        Position startPos = startRoom.getRandomFloorPosition();
        player.setPosition(startPos);

        statistics.levelsCompleted++;
    }

    public boolean useItem(ItemType type, int slot) {
        Item item = player.useItem(type, slot);
        if (item != null && item instanceof Consumable) {
            // Item already consumed in useItem method
            return true;
        }
        return false;
    }

    public List<Item> getInventory(ItemType type) {
        return player.getItemStack(type);
    }

    public boolean isGameOver() { return isGameOver; }
    public boolean isVictory() { return isVictory; }
    public Player getPlayer() { return player; }
    public int getCurrentLevelIndex() { return currentLevelIndex + 1; }
    public GameStatistics getStatistics() { return statistics; }

    public static class GameStatistics {
        public int enemiesDefeated = 0;
        public int itemsCollected = 0;
        public int totalGoldCollected = 0;
        public int cellsMoved = 0;
        public int totalAttacks = 0;
        public int missedAttacks = 0;
        public int totalDamageDealt = 0;
        public int totalDamageTaken = 0;
        public int levelsCompleted = 0;

        public String getSummary() {
            return String.format(
                    "Levels: %d | Enemies: %d | Gold: %d | Items: %d | Moves: %d\n" +
                            "Attacks: %d | Hits: %d | Damage Dealt: %d | Damage Taken: %d",
                    levelsCompleted, enemiesDefeated, totalGoldCollected, itemsCollected,
                    cellsMoved, totalAttacks, totalAttacks - missedAttacks,
                    totalDamageDealt, totalDamageTaken
            );
        }
    }
}