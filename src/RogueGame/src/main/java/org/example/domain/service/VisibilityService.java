package org.example.domain.service;

import org.example.domain.model.entity.*;

/**
 * Сервис отвечающий за расчет видимости (туман войны). Использует алгоритмы Ray Casting и
 * Брезенхема для определения, какие клетки видит игрок в текущий момент.
 */
public class VisibilityService {

    // Радиус обзора игрока (в клетках)
    private static final int VIEW_RADIUS = 12;
    private boolean fogOfWarEnabled = true;

    private final LevelService levelService;

    public VisibilityService(LevelService levelService) {
        this.levelService = levelService;
    }

    /**
     * Рассчитывает видимость для текущего уровня. Вызывается каждый ход перед рендерингом.
     *
     * @param level текущий уровень
     * @param playerPosition позиция игрока
     */
    public void calculateFov(Level level, Position playerPosition) {
        if (!fogOfWarEnabled) {
            return;
        }

        TileMap tileMap = level.getTileMap();

        // Сбрасываем видимость для всех клеток
        resetVisibility(tileMap);

        // Получаем комнату, в которой находится игрок
        Room currentRoom = levelService.findRoomAt(level, playerPosition);

        // 1. Если игрок в комнате - вся комната видима
        if (currentRoom != null) {
            revealRoom(currentRoom, tileMap, true);
        }

        // 2. Если игрок в коридоре - используем ray casting
        else {
            // Делаем видимыми клетки в коридоре вокруг игрока
            revealCorridorArea(playerPosition, tileMap, level);

            // Ray casting для коридоров и соседних комнат
            calculateRayCasting(playerPosition, tileMap, level);
        }

        // 3. Отмечаем просмотренные клетки (wasVisited)
        markVisitedTiles(tileMap);
    }

    public void setFogOfWarEnabled(boolean enabled) {
        this.fogOfWarEnabled = enabled;
    }

    public boolean isFogOfWarEnabled() {
        return fogOfWarEnabled;
    }

    /** Делает всю карту полностью видимой (когда туман войны выключен). */
    public void revealAll(Level level) {
        TileMap tileMap = level.getTileMap();
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null) {
                    tile.setVisible(true);
                }
            }
        }
    }

    // Сохраняем состояние wasVisited
    private boolean[][] savedVisited;

    /** Сохраняет текущее состояние wasVisited всех клеток. */
    public void saveVisitedState(Level level) {
        TileMap tileMap = level.getTileMap();
        savedVisited = new boolean[tileMap.getHeight()][tileMap.getWidth()];
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null) {
                    savedVisited[y][x] = tile.wasVisited();
                }
            }
        }
    }

    /** Восстанавливает сохраненное состояние wasVisited. */
    public void restoreVisitedState(Level level) {
        if (savedVisited == null) return;

        TileMap tileMap = level.getTileMap();
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null && y < savedVisited.length && x < savedVisited[y].length) {
                    tile.setWasVisited(savedVisited[y][x]);
                }
            }
        }
    }

    // Сохраняем состояние видимости перед выключением тумана
    private boolean[][] savedVisibility;

    /** Сохраняет текущее состояние видимости всех клеток. */
    public void saveVisibilityState(Level level) {
        TileMap tileMap = level.getTileMap();
        savedVisibility = new boolean[tileMap.getHeight()][tileMap.getWidth()];
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null) {
                    savedVisibility[y][x] = tile.isVisible();
                }
            }
        }
    }

    /** Восстанавливает сохраненное состояние видимости. */
    public void restoreVisibilityState(Level level) {
        if (savedVisibility == null) return;

        TileMap tileMap = level.getTileMap();
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null && y < savedVisibility.length && x < savedVisibility[y].length) {
                    tile.setVisible(savedVisibility[y][x]);
                }
            }
        }
    }

    /** Сбрасывает флаг видимости для всех клеток. */
    private void resetVisibility(TileMap tileMap) {
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null) {
                    tile.setVisible(false);
                }
            }
        }
    }

    /** Отмечает просмотренные клетки (для отображения стен в посещенных комнатах). */
    private void markVisitedTiles(TileMap tileMap) {
        for (int y = 0; y < tileMap.getHeight(); y++) {
            for (int x = 0; x < tileMap.getWidth(); x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null && tile.isVisible()) {
                    tile.setWasVisited(true);
                }
            }
        }
    }

    /** Делает всю комнату видимой. */
    private void revealRoom(Room room, TileMap tileMap, boolean visible) {
        // Получаем границы комнаты
        Position topLeft = room.getTopLeft();
        int width = room.getWidth();
        int height = room.getHeight();

        // Проходим по всем клеткам в границах комнаты
        for (int y = topLeft.y(); y < topLeft.y() + height; y++) {
            for (int x = topLeft.x(); x < topLeft.x() + width; x++) {
                Tile tile = tileMap.getTile(x, y);
                if (tile != null) {
                    tile.setVisible(visible);
                }
            }
        }
    }

    /** Делает видимой область коридора вокруг игрока. */
    private void revealCorridorArea(Position playerPos, TileMap tileMap, Level level) {
        // Радиус видимости в коридоре - 3 клетки
        int corridorViewRadius = 3;

        for (int dy = -corridorViewRadius; dy <= corridorViewRadius; dy++) {
            for (int dx = -corridorViewRadius; dx <= corridorViewRadius; dx++) {
                int x = playerPos.x() + dx;
                int y = playerPos.y() + dy;

                // Проверяем расстояние (манхэттенское)
                if (Math.abs(dx) + Math.abs(dy) > corridorViewRadius) {
                    continue;
                }

                if (tileMap.isWithinBounds(x, y)) {
                    Tile tile = tileMap.getTile(x, y);
                    if (tile != null && tile.isWalkable()) {
                        tile.setVisible(true);

                        // Проверяем, есть ли рядом вход в комнату
                        checkAdjacentRoom(x, y, tileMap, level);
                    }
                }
            }
        }
    }

    /** Проверяет, есть ли рядом с коридором комната, и делает её частично видимой. */
    private void checkAdjacentRoom(int x, int y, TileMap tileMap, Level level) {
        // Проверяем все соседние клетки
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (tileMap.isWithinBounds(nx, ny)) {
                Tile tile = tileMap.getTile(nx, ny);
                if (tile != null && tile.isFloor()) { // Пол комнаты
                    // Нашли вход в комнату - делаем видимыми клетки у входа
                    revealRoomEntrance(new Position(nx, ny), tileMap, level);
                }
            }
        }
    }

    /** Делает видимыми клетки у входа в комнату. */
    private void revealRoomEntrance(Position entrance, TileMap tileMap, Level level) {
        Room room = levelService.findRoomAt(level, entrance);
        if (room == null) return;

        Position topLeft = room.getTopLeft();
        int width = room.getWidth();
        int height = room.getHeight();

        // Делаем видимыми только клетки рядом с входом (радиус 3)
        int entranceX = entrance.x();
        int entranceY = entrance.y();

        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                // Проверяем расстояние
                if (Math.abs(dx) + Math.abs(dy) > 3) continue;

                int x = entranceX + dx;
                int y = entranceY + dy;

                // Проверяем, что клетка в пределах комнаты
                if (x >= topLeft.x()
                        && x < topLeft.x() + width
                        && y >= topLeft.y()
                        && y < topLeft.y() + height) {

                    if (tileMap.isWithinBounds(x, y)) {
                        Tile tile = tileMap.getTile(x, y);
                        if (tile != null) {
                            tile.setVisible(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Рассчитывает видимость с помощью Ray Casting. Использует алгоритм Брезенхема для пуска лучей во
     * все стороны.
     */
    private void calculateRayCasting(Position start, TileMap tileMap, Level level) {
        // Пускаем лучи во все стороны с шагом 2 градуса для производительности
        for (int angle = 0; angle < 360; angle += 2) {
            castRay(start, angle, tileMap, level);
        }

        // Дополнительно проверяем все клетки в радиусе видимости
        // для гарантии, что ничего не пропустили
        for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
            for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
                // Проверяем расстояние (евклидово для более естественного круга)
                if (dx * dx + dy * dy > VIEW_RADIUS * VIEW_RADIUS) {
                    continue;
                }

                int x = start.x() + dx;
                int y = start.y() + dy;

                if (tileMap.isWithinBounds(x, y)) {
                    // Проверяем прямую видимость с помощью Брезенхема
                    if (hasLineOfSight(start, new Position(x, y), tileMap, level)) {
                        Tile tile = tileMap.getTile(x, y);
                        if (tile != null) {
                            tile.setVisible(true);
                        }
                    }
                }
            }
        }
    }

    /** Пускает луч в заданном направлении. Использует алгоритм Брезенхема для рисования линии. */
    private void castRay(Position start, int angle, TileMap tileMap, Level level) {
        double radians = Math.toRadians(angle);
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);

        int x = start.x();
        int y = start.y();

        // Идем по лучу до границы видимости или до стены
        for (int step = 1; step <= VIEW_RADIUS; step++) {
            // Вычисляем следующую клетку
            int nextX = (int) Math.round(x + dx * step);
            int nextY = (int) Math.round(y + dy * step);

            if (!tileMap.isWithinBounds(nextX, nextY)) {
                break; // Достигли границы карты
            }

            Tile tile = tileMap.getTile(nextX, nextY);
            if (tile == null) continue;

            // Делаем клетку видимой
            tile.setVisible(true);

            // Если наткнулись на стену, луч останавливается
            if (tile.isWall()) {
                break;
            }

            // Если вошли в другую комнату через коридор, продолжаем
            // (луч проходит через комнату)
        }
    }

    /**
     * Проверяет наличие прямой видимости между двумя точками. Использует алгоритм Брезенхема.
     *
     * @param from начальная точка
     * @param to целевая точка
     * @param tileMap карта уровня
     * @return true если есть прямая видимость
     */
    private boolean hasLineOfSight(Position from, Position to, TileMap tileMap, Level level) {
        int x0 = from.x();
        int y0 = from.y();
        int x1 = to.x();
        int y1 = to.y();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int x = x0;
        int y = y0;

        // Проходим по линии от начальной до конечной точки
        while (x != x1 || y != y1) {
            // Проверяем текущую клетку (кроме начальной)
            if (x != x0 || y != y0) {
                if (!isTransparent(x, y, tileMap, level, from, to)) {
                    return false; // Наткнулись на непрозрачную клетку
                }
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return true;
    }

    /** Проверяет, является ли клетка прозрачной для луча зрения. */
    private boolean isTransparent(
            int x, int y, TileMap tileMap, Level level, Position from, Position to) {
        Tile tile = tileMap.getTile(x, y);
        if (tile == null) return false;

        // Пропускаем начальную и конечную точки
        if ((x == from.x() && y == from.y()) || (x == to.x() && y == to.y())) {
            return true;
        }

        // Проверяем тип клетки
        if (tile.isWall()) {
            return false; // Стены непрозрачны
        }

        return true; // Пол и коридоры прозрачны
    }

    /**
     * Обновляет видимость после перехода на новый уровень. Сбрасывает видимость, но сохраняет
     * просмотренные клетки.
     */
    public void initializeLevel(Level level, Position playerPos) {
        TileMap tileMap = level.getTileMap();

        // Сбрасываем видимость
        resetVisibility(tileMap);

        // Делаем видимой стартовую комнату
        Room startRoom = levelService.findRoomAt(level, playerPos);
        if (startRoom != null) {
            revealRoom(startRoom, tileMap, true);
        } else {
            // Если игрок не в комнате (в коридоре), делаем видимой область вокруг
            revealCorridorArea(playerPos, tileMap, level);
        }

        // Рассчитываем видимость
        calculateFov(level, playerPos);
    }
}
