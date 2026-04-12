package org.example.domain.service;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.*;

/**
 * Сервис отвечающий за перемещение сущностей по игровому миру, проверку коллизий и обработку
 * результатов перемещения.
 */
public class MovementService {

    private final CombatService combatService;
    private final ItemService itemService;
    private final StatisticsService statisticsService;
    private final DoorService doorService;
    private final DifficultyBalanceService difficultyBalanceService;

    /** Конструктор с внедрением DifficultyBalanceService. */
    public MovementService(
            CombatService combatService,
            ItemService itemService,
            LevelService levelService,
            StatisticsService statisticsService,
            DifficultyBalanceService difficultyBalanceService) {
        this.combatService = combatService;
        this.itemService = itemService;
        this.statisticsService = statisticsService;
        this.doorService = new DoorService();
        this.difficultyBalanceService = difficultyBalanceService;
    }

    /** Устанавливает ID текущей сессии для регистрации метрик. */
    public void setCurrentSessionId(long sessionId) {
        if (combatService != null) {
            combatService.setCurrentSessionId(sessionId);
        }
    }

    /** Проверяет, может ли персонаж переместиться в указанном направлении. */
    public boolean canMove(
            org.example.domain.model.entity.Character character,
            Direction direction,
            Level level,
            boolean isAttacking) {
        if (direction.isNone()) return false;

        Position currentPos = new Position(character.getX(), character.getY());
        Position newPos = calculateNewPosition(currentPos, direction);

        if (!isWithinBounds(newPos, level)) {
            return false;
        }

        Tile targetTile = level.getTileMap().getTile(newPos);

        if (!targetTile.isWalkable()) {
            return false;
        }

        // Проверка на дверь
        Door door = level.getDoorAt(newPos.x(), newPos.y());
        if (door != null && door.isLocked()) {
            // Если это атака или игрок не может открыть дверь, движение невозможно
            if (!isAttacking) {
                // Для монстров закрытые двери непроходимы
                if (character instanceof Monster) {
                    return false;
                }
                // Для игрока - будет обработано в resolvePlayerMovement
            }
        }

        if (targetTile.hasMonster()) {
            if (isAttacking) {
                return true;
            }
            return false;
        }

        if (character instanceof Monster && level.getTileMap().isPlayerAt(newPos)) {
            return isAttacking;
        }

        return true;
    }

    /** Обрабатывает перемещение игрока в указанном направлении. */
    public MoveResult resolvePlayerMovement(
            Player player, Direction direction, Level level, long sessionId) {
        // Регистрируем шаг в системе балансировки
        if (direction != Direction.WAIT && difficultyBalanceService != null) {
            difficultyBalanceService.registerStep();
        }

        // Обработка команды ожидания
        if (direction == Direction.WAIT) {
            return new MoveResult(MoveResultType.WAIT, "Ожидание");
        }

        Position currentPos = new Position(player.getX(), player.getY());
        Position newPos = calculateNewPosition(currentPos, direction);

        // Проверка границ карты
        if (!isWithinBounds(newPos, level)) {
            return new MoveResult(MoveResultType.BLOCKED, "Край карты");
        }

        Tile targetTile = level.getTileMap().getTile(newPos);

        // Проверка стены
        if (!targetTile.isWalkable()) {
            return new MoveResult(MoveResultType.BLOCKED, "Стена!");
        }

        Door door = level.getDoorAt(newPos.x(), newPos.y());
        MoveResult DOOR_OPENED = resolveDoorInteraction(player, level, door, currentPos, newPos);
        if (DOOR_OPENED != null) return DOOR_OPENED;

        if (targetTile.hasMonster()) {
            Monster monster = targetTile.getMonster();

            // Если это невидимый мимик, раскрываем его
            if (monster.getMonsterType() == MonsterType.MIMIC && monster.isInvisible()) {
                monster.setInvisible(false);
            }

            CombatResult combatResult = combatService.processAttack(player, monster);

            if (combatResult.isMonsterDefeated()) {
                return resolveMonsterDefeated(level, sessionId, monster, newPos, combatResult);
            } else {
                return new MoveResult(MoveResultType.ATTACK_HIT, combatResult, monster);
            }
        }

        if (targetTile.hasItem()) {
            return resolveItemInteraction(player, level, sessionId, targetTile, currentPos, newPos);
        }

        if (targetTile.isExit()) {
            movePlayer(player, currentPos, newPos, level);
            return new MoveResult(MoveResultType.NEXT_LEVEL, "Переход на следующий уровень");
        }

        movePlayer(player, currentPos, newPos, level);
        return new MoveResult(MoveResultType.MOVE, (String) null);
    }

    private MoveResult resolveMonsterDefeated(
            Level level, long sessionId, Monster monster, Position newPos, CombatResult combatResult) {
        level.removeMonster(monster);
        int treasureValue = combatService.calculateTreasureDrop(monster, level.getLevelNumber());

        if (difficultyBalanceService != null) {
            difficultyBalanceService.registerMonsterKilled(monster.getMonsterType(), treasureValue);
        }

        statisticsService.updateEnemyDefeated(sessionId, monster.getMonsterType(), treasureValue);

        Treasure treasure = createTreasureByValue(treasureValue);
        level.addItem(treasure, newPos.x(), newPos.y());
        return new MoveResult(MoveResultType.ATTACK_KILL, combatResult, monster);
    }

    private MoveResult resolveItemInteraction(
            Player player,
            Level level,
            long sessionId,
            Tile targetTile,
            Position currentPos,
            Position newPos) {
        Item item = targetTile.getItem();

        boolean canPickup = canPickupItem(player, item);

        if (canPickup) {
            level.removeItem(item);
            boolean added = itemService.addToBackpack(player, item, sessionId);

            if (added) {
                movePlayer(player, currentPos, newPos, level);

                // Специальное сообщение для ключей
                if (item instanceof Key) {
                    Key key = (Key) item;
                    return new MoveResult(
                            MoveResultType.KEY_FOUND, String.format("Подобран %s!", key.getName()));
                }

                return new MoveResult(MoveResultType.PICKUP, item);
            } else {
                level.addItem(item, newPos.x(), newPos.y());
                return new MoveResult(MoveResultType.BLOCKED, "Не удалось подобрать предмет");
            }
        } else {
            movePlayer(player, currentPos, newPos, level);
            return new MoveResult(MoveResultType.MOVE, "Рюкзак полон, предмет не подобран");
        }
    }

    private MoveResult resolveDoorInteraction(
            Player player, Level level, Door door, Position currentPos, Position newPos) {
        if (door != null && door.isLocked()) {
            // Проверяем, есть ли у игрока ключ нужного цвета
            KeyColor doorColor = door.getColor();
            if (doorService.canOpenDoor(player, doorColor)) {
                // Открываем дверь
                doorService.openDoor(door);

                // Удаляем ключ из рюкзака
                doorService.useKey(player, doorColor);

                // Перемещаем игрока
                movePlayer(player, currentPos, newPos, level);

                return new MoveResult(
                        MoveResultType.DOOR_OPENED,
                        String.format("Дверь открыта ключом %s!", doorColor.getDisplayName()));
            } else {
                // Нет ключа - не можем пройти
                return new MoveResult(
                        MoveResultType.DOOR_LOCKED,
                        String.format("Дверь заперта! Нужен %s ключ.", doorColor.getDisplayName()));
            }
        }
        return null;
    }

    /** Обрабатывает перемещение монстра. */
    public MoveResult resolveMonsterMovement(
            Monster monster, Direction direction, Level level, Player player) {
        Position currentPos = new Position(monster.getX(), monster.getY());
        Position newPos = calculateNewPosition(currentPos, direction);

        if (!isWithinBounds(newPos, level)) {
            return new MoveResult(
                    MoveResultType.MONSTER_BLOCKED,
                    String.format("%s упёрся в стену", monster.getMonsterType().name()),
                    monster);
        }

        Tile targetTile = level.getTileMap().getTile(newPos);

        if (!(targetTile.isFloor() || targetTile.isCorridor() || targetTile.isExit())) {
            // Проверяем, не дверь ли это
            Door door = level.getDoorAt(newPos.x(), newPos.y());
            if (door == null || door.isLocked()) {
                return new MoveResult(MoveResultType.BLOCKED, "Стена");
            }
            // Если дверь открыта - монстр может пройти
            if (!door.isOpen()) {
                return new MoveResult(MoveResultType.BLOCKED, "Стена");
            }
        }

        // СЛУЧАЙ 1: Монстр атакует игрока
        if (level.getTileMap().isPlayerAt(newPos)) {
            return resolveMonsterAttack(monster, player);
        }

        // СЛУЧАЙ 2: Монстр перемещается на пустую клетку
        if (!targetTile.hasMonster() && (targetTile.isWalkable() || isOpenDoorAt(level, newPos))) {
            return resolveMonsterMovement(monster, level, currentPos, newPos);
        }

        return new MoveResult(
                MoveResultType.MONSTER_BLOCKED,
                String.format("%s не может двигаться", monster.getMonsterType().name()),
                monster);
    }

    private MoveResult resolveMonsterMovement(
            Monster monster, Level level, Position currentPos, Position newPos) {
        moveMonster(monster, currentPos, newPos, level);

        if (monster.getMonsterType() == MonsterType.GHOST && !monster.isInvisible()) {
            if (random.nextDouble() < 0.3) {
                monster.setInvisible(true);
            }
        }

        return new MoveResult(MoveResultType.MONSTER_MOVE, (String) null, monster);
    }

    private MoveResult resolveMonsterAttack(Monster monster, Player player) {
        CombatResult combatResult = combatService.processAttack(monster, player);

        if (monster.getMonsterType() == MonsterType.OGRE) {
            monster.processAttack();
        }

        return new MoveResult(MoveResultType.MONSTER_ATTACK, combatResult, monster);
    }

    /** Проверяет, является ли клетка открытой дверью. */
    private boolean isOpenDoorAt(Level level, Position pos) {
        Door door = level.getDoorAt(pos.x(), pos.y());
        return door != null && door.isOpen();
    }

    /** Перемещает игрока. */
    private void movePlayer(Player player, Position from, Position to, Level level) {
        TileMap tileMap = level.getTileMap();
        tileMap.setPlayerPosition(to.x(), to.y());
        player.moveTo(to.x(), to.y());

        Tile tile = tileMap.getTile(to);
        tile.setWasVisited(true);
    }

    /** Перемещает монстра. */
    private void moveMonster(Monster monster, Position from, Position to, Level level) {
        TileMap tileMap = level.getTileMap();
        tileMap.removeMonster(from.x(), from.y());
        tileMap.setMonster(to.x(), to.y(), monster);
    }

    /** Проверяет, может ли игрок подобрать предмет. */
    private boolean canPickupItem(Player player, Item item) {
        Backpack backpack = player.getBackpack();
        if (backpack == null) {
            return false;
        }
        return backpack.hasSpaceFor(item.getType());
    }

    /** Получает список доступных направлений для перемещения. */
    public List<Direction> getAvailableDirections(
            org.example.domain.model.entity.Character character,
            Level level,
            boolean ignoreMonsters,
            boolean allowDiagonal) {
        List<Direction> available = new ArrayList<>();
        Position currentPos = new Position(character.getX(), character.getY());

        Direction[] directions =
                allowDiagonal ? Direction.movableDirections() : Direction.cardinalDirections();

        for (Direction dir : directions) {
            if (dir.isNone()) continue;

            Position newPos = calculateNewPosition(currentPos, dir);

            if (!isWithinBounds(newPos, level)) continue;

            Tile tile = level.getTileMap().getTile(newPos);

            // Проверка на дверь
            Door door = level.getDoorAt(newPos.x(), newPos.y());
            if (door != null && door.isLocked()) {
                // Для монстров закрытые двери недоступны
                if (character instanceof Monster) {
                    continue;
                }
                // Для игрока - доступно, но будет обработано отдельно
                if (character instanceof Player) {
                    available.add(dir);
                    continue;
                }
            }

            if (!tile.isWalkable()) continue;

            if (!ignoreMonsters && tile.hasMonster()) continue;

            if (character instanceof Monster && level.getTileMap().isPlayerAt(newPos)) {
                continue;
            }

            available.add(dir);
        }

        return available;
    }

    /** Находит кратчайший путь от одной позиции до другой (BFS). */
    public Direction findPathStep(
            Position start, Position target, Level level, boolean allowDiagonal) {
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        cameFrom.put(start, null);

        Direction[] directions =
                allowDiagonal ? Direction.movableDirections() : Direction.cardinalDirections();

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(target)) {
                return reconstructFirstStep(current, start, cameFrom);
            }

            for (Direction dir : directions) {
                Position next = calculateNewPosition(current, dir);

                if (!isWithinBounds(next, level)) continue;
                if (visited.contains(next)) continue;

                Tile tile = level.getTileMap().getTile(next);

                if (!tile.isWalkable()) {
                    // Проверяем, не открытая ли это дверь
                    Door door = level.getDoorAt(next.x(), next.y());
                    if (door == null || !door.isOpen()) {
                        continue;
                    }
                }

                if (tile.hasMonster() && !next.equals(target)) {
                    continue;
                }

                if (level.getTileMap().isPlayerAt(next) && !next.equals(target)) {
                    continue;
                }

                visited.add(next);
                cameFrom.put(next, current);
                queue.add(next);
            }
        }

        return null;
    }

    /** Вычисляет новую позицию. */
    private Position calculateNewPosition(Position current, Direction direction) {
        return new Position(current.x() + direction.getDx(), current.y() + direction.getDy());
    }

    /** Проверяет, находится ли позиция в границах карты. */
    private boolean isWithinBounds(Position pos, Level level) {
        return level.getTileMap().isWithinBounds(pos);
    }

    /** Восстанавливает первый шаг из пути, найденного BFS. */
    private Direction reconstructFirstStep(
            Position target, Position start, Map<Position, Position> cameFrom) {
        Position current = target;
        Position previous = cameFrom.get(current);

        while (previous != null && !previous.equals(start)) {
            current = previous;
            previous = cameFrom.get(current);
        }

        return getDirectionBetween(start, current);
    }

    /** Определяет направление между двумя соседними позициями. */
    private Direction getDirectionBetween(Position from, Position to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        return Direction.fromDelta(dx, dy);
    }

    /** Создаёт сокровище подходящей стоимости. */
    private Treasure createTreasureByValue(int value) {
        if (value >= 100) return new Treasure(ItemSubType.GEM);
        if (value >= 50) return new Treasure(ItemSubType.GOLD_BAR);
        if (value >= 10) return new Treasure(ItemSubType.GOLD_COIN);
        if (value >= 5) return new Treasure(ItemSubType.SILVER_BAR);
        return new Treasure(ItemSubType.SILVER_COIN);
    }

    private final Random random = new Random();
}
