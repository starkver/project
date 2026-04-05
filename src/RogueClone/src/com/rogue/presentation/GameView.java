package com.rogue.presentation;

import com.rogue.domain.*;
import com.rogue.domain.entities.*;
import com.rogue.domain.items.*;
import com.rogue.data.SaveManager;
import java.util.*;

public class GameView {
    private GameSession session;
    private Scanner scanner;
    private boolean running;
    private SaveManager saveManager;

    // Colors for console rendering (using ANSI codes)
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String WHITE = "\u001B[37m";
    private static final String CYAN = "\u001B[36m";

    public GameView(GameSession session) {
        this.session = session;
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.saveManager = new SaveManager();
    }

    public void start() {
        clearScreen();
        printWelcomeMessage();

        while (running && !session.isGameOver()) {
            render();
            handleInput();

            if (session.isGameOver()) {
                break;
            }
        }

        if (session.isVictory()) {
            printVictory();
        } else if (session.isGameOver()) {
            printGameOver();
        }

        saveManager.saveLeaderboard(session);
        printStatistics();
    }

    private void render() {
        clearScreen();
        Level level = session.getCurrentLevel();
        Player player = session.getPlayer();

        // Render game map
        System.out.println(CYAN + "═".repeat(80) + RESET);
        System.out.println(YELLOW + "Rogue Clone - Level " + session.getCurrentLevelIndex() + RESET);
        System.out.println(CYAN + "═".repeat(80) + RESET);

        // Calculate viewport (20x20 around player)
        int viewX = player.getPosition().x;
        int viewY = player.getPosition().y;

        for (int y = viewY - 10; y <= viewY + 9; y++) {
            for (int x = viewX - 20; x <= viewX + 19; x++) {
                Position pos = new Position(x, y);

                if (Math.abs(x - viewX) > 15 || Math.abs(y - viewY) > 8) {
                    System.out.print("  ");
                    continue;
                }

                // Check what's at this position
                if (player.getPosition().equals(pos)) {
                    System.out.print(GREEN + "@ " + RESET);
                } else if (level.getEnemyAt(pos) != null) {
                    Enemy enemy = level.getEnemyAt(pos);
                    String color = getEnemyColor(enemy);
                    System.out.print(color + enemy.getSymbol() + " " + RESET);
                } else if (level.getItemAt(pos) != null) {
                    Item item = level.getItemAt(pos);
                    System.out.print(YELLOW + item.getSymbol() + " " + RESET);
                } else if (level.isWall(pos)) {
                    System.out.print("# ");
                } else if (level.isWalkable(pos)) {
                    System.out.print(". ");
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }

        // Render UI panel
        System.out.println(CYAN + "═".repeat(80) + RESET);
        renderStatusPanel();
        System.out.println(CYAN + "═".repeat(80) + RESET);
        renderHelp();
    }

    private String getEnemyColor(Enemy enemy) {
        switch (enemy.getEnemyType()) {
            case ZOMBIE: return GREEN;
            case VAMPIRE: return RED;
            case GHOST: return WHITE;
            case OGRE: return YELLOW;
            default: return WHITE;
        }
    }

    private void renderStatusPanel() {
        Player player = session.getPlayer();
        GameSession.GameStatistics stats = session.getStatistics();

        System.out.printf("HP: %d/%d | STR: %d | DEX: %d | Gold: %d | Level: %d\n",
                player.getCurrentHealth(), player.getMaxHealth(),
                player.getStrength(), player.getDexterity(),
                player.getGold(), player.getLevel());

        System.out.printf("Enemies killed: %d | Items used: %d | Depth: %d\n",
                stats.enemiesDefeated, stats.itemsCollected, session.getCurrentLevelIndex());

        if (player.getWeapon() != null) {
            System.out.println("Weapon: " + player.getWeapon().getName());
        }
    }

    private void renderHelp() {
        System.out.println("Commands: WASD=Move | H=Weapons | J=Food | K=Potions | E=Scrolls | Q=Save&Quit");
    }

    private void renderInventory(ItemType type) {
        List<Item> items = session.getInventory(type);
        if (items.isEmpty()) {
            System.out.println("No " + type + " available!");
            waitForEnter();
            return;
        }

        System.out.println("\nSelect " + type + " to use (1-" + items.size() + ", 0=cancel):");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i+1) + ". " + items.get(i).getDescription());
        }

        String input = scanner.nextLine();
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= items.size()) {
                if (session.useItem(type, choice - 1)) {
                    System.out.println("Used " + type);
                }
            }
        } catch (NumberFormatException e) {
            // Invalid input, ignore
        }

        waitForEnter();
    }

    private void handleInput() {
        String input = scanner.nextLine().toLowerCase();

        switch (input) {
            case "w":
                session.movePlayer(Direction.UP);
                break;
            case "s":
                session.movePlayer(Direction.DOWN);
                break;
            case "a":
                session.movePlayer(Direction.LEFT);
                break;
            case "d":
                session.movePlayer(Direction.RIGHT);
                break;
            case "h":
                renderInventory(ItemType.WEAPON);
                break;
            case "j":
                renderInventory(ItemType.FOOD);
                break;
            case "k":
                renderInventory(ItemType.ELIXIR);
                break;
            case "e":
                renderInventory(ItemType.SCROLL);
                break;
            case "q":
                saveManager.saveGame(session);
                running = false;
                break;
            default:
                // Invalid command
                break;
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void printWelcomeMessage() {
        System.out.println(YELLOW + """
        
        ╔══════════════════════════════════════════════════════════════╗
        ║                    WELCOME TO ROGUE CLONE                    ║
        ║                                                                ║
        ║  Descend through 21 levels of randomly generated dungeons.    ║
        ║  Defeat monsters, collect treasure, and find the exit!        ║
        ║                                                                ║
        ║  Controls: WASD - Move                                        ║
        ║            H   - Use weapons                                  ║
        ║            J   - Eat food                                     ║
        ║            K   - Drink potions                                ║
        ║            E   - Read scrolls                                 ║
        ║            Q   - Save and quit                                ║
        ║                                                                ║
        ║                    Press Enter to begin...                    ║
        ╚══════════════════════════════════════════════════════════════╝
        
        """ + RESET);
        scanner.nextLine();
    }

    private void printGameOver() {
        System.out.println(RED + """
        
        ╔══════════════════════════════════════════════════════════════╗
        ║                      GAME OVER!                              ║
        ║                                                                ║
        ║                   You have fallen in battle...                ║
        ║                                                                ║
        ║                    Better luck next time!                     ║
        ╚══════════════════════════════════════════════════════════════╝
        
        """ + RESET);
    }

    private void printVictory() {
        System.out.println(YELLOW + """
        
        ╔══════════════════════════════════════════════════════════════╗
        ║                      VICTORY!                                ║
        ║                                                                ║
        ║          Congratulations! You have conquered the              ║
        ║                depths and emerged victorious!                 ║
        ║                                                                ║
        ║                   You are a true hero!                        ║
        ╚══════════════════════════════════════════════════════════════╝
        
        """ + RESET);
    }

    private void printStatistics() {
        System.out.println(CYAN + "\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    FINAL STATISTICS                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        GameSession.GameStatistics stats = session.getStatistics();
        System.out.printf("║  Levels Completed:  %-40d ║\n", stats.levelsCompleted);
        System.out.printf("║  Enemies Defeated:  %-40d ║\n", stats.enemiesDefeated);
        System.out.printf("║  Gold Collected:    %-40d ║\n", stats.totalGoldCollected);
        System.out.printf("║  Items Collected:   %-40d ║\n", stats.itemsCollected);
        System.out.printf("║  Cells Moved:       %-40d ║\n", stats.cellsMoved);
        System.out.printf("║  Total Attacks:     %-40d ║\n", stats.totalAttacks);
        System.out.printf("║  Hits Landed:       %-40d ║\n", stats.totalAttacks - stats.missedAttacks);
        System.out.printf("║  Damage Dealt:      %-40d ║\n", stats.totalDamageDealt);
        System.out.printf("║  Damage Taken:      %-40d ║\n", stats.totalDamageTaken);

        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);

        waitForEnter();
    }
}