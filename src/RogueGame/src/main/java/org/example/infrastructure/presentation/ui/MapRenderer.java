package org.example.infrastructure.presentation.ui;

import static org.example.domain.model.enums.TileType.CORRIDOR;
import static org.example.domain.model.enums.TileType.FLOOR;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.MonsterType;
import org.example.domain.model.enums.TileType;
import org.example.domain.service.VisibilityService;

public class MapRenderer {

    private static final int MAP_LEFT = 21;
    private static final int MAP_TOP = 0;
    private static final int MAP_WIDTH = 99;
    private static final int MAP_HEIGHT = 30;

    public void draw(Screen screen, Level level, VisibilityService visibilityService) {
        TileMap tileMap = level.getTileMap();

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                int worldX = x;
                int worldY = y;

                // Проверка границ мира
                if (worldX >= tileMap.getWidth() || worldY >= tileMap.getHeight()) {
                    drawTile(screen, MAP_LEFT + x, MAP_TOP + y, ' ', TextColor.ANSI.WHITE);
                    continue;
                }

                Tile tile = tileMap.getTile(worldX, worldY);
                if (tile == null) {
                    drawTile(screen, MAP_LEFT + x, MAP_TOP + y, ' ', TextColor.ANSI.WHITE);
                    continue;
                }

                // Определяем, видна ли клетка
                boolean isVisible = tile.isVisible();
                boolean wasVisited = tile.wasVisited();
                boolean fogOfWarEnabled =
                        visibilityService != null && visibilityService.isFogOfWarEnabled();

                char symbol;
                TextColor.ANSI color;

                // Если туман войны выключен или клетка видима
                if (!fogOfWarEnabled || isVisible) {
                    symbol = getTileSymbol(level, worldX, worldY, tile);
                    color = getTileColor(level, worldX, worldY, tile);
                }
                // Если клетка была посещена, но сейчас не видима (туман войны включен)
                else if (wasVisited) {
                    // Показываем стены и пол красным цветом
                    if (tile.isWall()) {
                        symbol = getWallSymbol();
                    } else {
                        symbol = getFloorSymbol();
                    }
                    color = TextColor.ANSI.RED;
                }
                // Клетка не исследована и не видима
                else {
                    symbol = ' '; // пустота
                    color = TextColor.ANSI.BLACK;
                }

                drawTile(screen, MAP_LEFT + x, MAP_TOP + y, symbol, color);
            }
        }
    }

    private char getWallSymbol() {
        return '#';
    }

    private char getFloorSymbol() {
        return '.';
    }

    private char getTileSymbol(Level level, int x, int y, Tile tile) {
        TileMap tileMap = level.getTileMap();

        if (!tile.isVisible() && !tile.wasVisited()) {
            return ' ';
        }

        if (!tile.isVisible() && tile.wasVisited()) {
            if (tile.isWall()) {
                return '#';
            }
            return ' ';
        }

        if (tileMap.isPlayerAt(x, y)) return '@';
        if (level.isExit(x, y)) return '↓';

        Monster monster = tileMap.getMonster(x, y);
        if (monster != null) {
            return setMonsterTile(monster);
        }

        Door door = level.getDoorAt(x, y);
        if (door != null) {
            return setDoorTile(door);
        }

        Item item = tileMap.getItem(x, y);
        if (item != null) {
            if (item instanceof Key) {
                return 'k';
            }
            return '?';
        }

        return switch (tile.getTileType()) {
            case WALL -> {
                if (hasAdjacentFloor(tileMap, x, y)) {
                    yield '#'; // показываем только "реальные" стены
                } else {
                    yield ' '; // скрываем лишние
                }
            }
            default -> tile.getTileType().getSymbol();
        };
    }

    private static char setMonsterTile(Monster monster) {
        // Для невидимого мимика показываем символ предмета
        if (monster.getMonsterType() == MonsterType.MIMIC && monster.isInvisible()) {
            return '?';
        }
        return monster.getMonsterType().getSymbol();
    }

    private static char setDoorTile(Door door) {
        if (door.isOpen()) {
            return '+';
        } else {
            // Запертая дверь - показываем букву цвета
            return switch (door.getColor()) {
                case RED -> 'R';
                case BLUE -> 'B';
                case YELLOW -> 'Y';
                case GREEN -> 'G';
                case PURPLE -> 'P';
            };
        }
    }

    private boolean hasAdjacentFloor(TileMap map, int x, int y) {
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (nx < 0 || ny < 0 || nx >= map.getWidth() || ny >= map.getHeight()) continue;

            TileType type = map.getTileType(nx, ny);
            if (type == FLOOR || type == CORRIDOR) {
                return true;
            }
        }
        return false;
    }

    private TextColor.ANSI getTileColor(Level level, int x, int y, Tile tile) {
        TileMap tileMap = level.getTileMap();

        if (!tile.isVisible() && tile.wasVisited()) {
            return TextColor.ANSI.RED;
        }

        if (!tile.isVisible()) {
            return TextColor.ANSI.BLACK;
        }

        if (tileMap.isPlayerAt(x, y)) return TextColor.ANSI.GREEN;

        Monster monster = tileMap.getMonster(x, y);
        if (monster != null) {
            return getMonsterColor(monster);
        }

        Door door = level.getDoorAt(x, y);

        if (door != null) {
            if (door.isOpen()) {
                return TextColor.ANSI.WHITE;
            }
            return switch (door.getColor()) {
                case RED -> TextColor.ANSI.RED;
                case BLUE -> TextColor.ANSI.BLUE;
                case YELLOW -> TextColor.ANSI.YELLOW;
                case GREEN -> TextColor.ANSI.GREEN;
                case PURPLE -> TextColor.ANSI.MAGENTA;
            };
        }

        Item item = tileMap.getItem(x, y);
        if (item != null) {
            if (item instanceof Key) {
                Key key = (Key) item;
                return switch (key.getColor()) {
                    case RED -> TextColor.ANSI.RED;
                    case BLUE -> TextColor.ANSI.BLUE;
                    case YELLOW -> TextColor.ANSI.YELLOW;
                    case GREEN -> TextColor.ANSI.GREEN;
                    case PURPLE -> TextColor.ANSI.MAGENTA;
                };
            }
            return TextColor.ANSI.CYAN;
        }

        return getWorldTileColor(tile);
    }

    private static TextColor.ANSI getWorldTileColor(Tile tile) {
        return switch (tile.getTileType()) {
            case FLOOR, WALL, CORRIDOR -> TextColor.ANSI.WHITE;
            case EXIT -> TextColor.ANSI.YELLOW;
            default -> TextColor.ANSI.WHITE;
        };
    }

    private static TextColor.ANSI getMonsterColor(Monster monster) {
        return switch (monster.getMonsterType()) {
            case ZOMBIE -> TextColor.ANSI.GREEN;
            case VAMPIRE -> TextColor.ANSI.RED;
            case GHOST -> TextColor.ANSI.CYAN;
            case OGRE -> TextColor.ANSI.YELLOW;
            case SNAKE_MAGE -> TextColor.ANSI.BLUE;
            case MIMIC -> TextColor.ANSI.MAGENTA;
        };
    }

    private void drawTile(Screen screen, int x, int y, char c, TextColor.ANSI color) {
        screen.setCharacter(x, y, new TextCharacter(c, color, TextColor.ANSI.BLACK));
    }
}
