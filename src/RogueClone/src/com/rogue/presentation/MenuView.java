package com.rogue.presentation;

import com.rogue.data.SaveManager;
import java.util.Scanner;

public class MenuView {
    private Scanner scanner;
    private SaveManager saveManager;

    public MenuView() {
        this.scanner = new Scanner(System.in);
        this.saveManager = new SaveManager();
    }

    public boolean showMainMenu() {
        clearScreen();

        System.out.println("""
        ╔══════════════════════════════════════════════════════════════╗
        ║                      ROGUE CLONE                             ║
        ║                      Version 1.0                             ║
        ╠══════════════════════════════════════════════════════════════╣
        ║                                                                ║
        ║                        1. New Game                            ║
        ║                        2. Continue                            ║
        ║                        3. Leaderboard                         ║
        ║                        4. Exit                                ║
        ║                                                                ║
        ╚══════════════════════════════════════════════════════════════╝
        """);

        System.out.print("\nChoose option: ");
        String input = scanner.nextLine();

        switch (input) {
            case "1":
                return false; // Start new game
            case "2":
                if (saveManager.hasSaveGame()) {
                    return true; // Continue game
                } else {
                    System.out.println("No save game found!");
                    waitForEnter();
                    return showMainMenu();
                }
            case "3":
                showLeaderboard();
                return showMainMenu();
            case "4":
                System.exit(0);
                return false;
            default:
                return showMainMenu();
        }
    }

    private void showLeaderboard() {
        clearScreen();
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      LEADERBOARD                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        var leaderboard = saveManager.loadLeaderboard();
        int rank = 1;

        for (var entry : leaderboard.getEntries()) {
            System.out.printf("║  %d.  Gold: %-8d  Level: %-6d  Kills: %-6d  ║\n",
                    rank++, entry.getGold(), entry.getLevel(), entry.getKills());
        }

        if (leaderboard.getEntries().isEmpty()) {
            System.out.println("║                    No scores yet!                            ║");
        }

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        waitForEnter();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}