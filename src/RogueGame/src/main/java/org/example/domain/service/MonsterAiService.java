package org.example.domain.service;

import java.util.*;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.*;

/**
 * Сервис отвечающий за искусственный интеллект монстров. Реализует уникальные паттерны движения для
 * каждого типа монстров и логику преследования игрока.
 */
public class MonsterAiService {

    private final Random random = new Random();
    private final MovementService movementService;
    private final CombatService combatService;
    private final LevelService levelService;

    // Хранилище состояний для каждого монстра (паттерны движения, таймеры и т.д.)
    private final Map<Monster, MonsterAiState> aiStates = new HashMap<>();

    public MonsterAiService(
            MovementService movementService, CombatService combatService, LevelService levelService) {
        this.movementService = movementService;
        this.combatService = combatService;
        this.levelService = levelService;
    }

    /**
     * Обрабатывает ход всех монстров на уровне. Вызывается из GameController после хода игрока.
     *
     * @param level текущий уровень
     * @param player игрок
     */
    public void processAllMonsters(Level level, Player player) {

        // Получаем всех живых монстров на уровне
        List<Monster> monsters = levelService.findAliveMonsters(level);
        List<Monster> mutableMonsters = new ArrayList<>(monsters);

        // Сортируем монстров для детерминированного порядка
        mutableMonsters.sort(Comparator.comparing(m -> m.getMonsterType().name()));

        for (Monster monster : mutableMonsters) {
            // Пропускаем монстров, которые отдыхают (например, огр после атаки)
            if (!combatService.canMonsterAttack(monster)) {
                continue;
            }

            processMonsterTurn(monster, level, player);
        }

        // Очищаем состояния мертвых монстров
        cleanupDeadMonsters();
    }

    /** Обрабатывает ход одного монстра. */
    private void processMonsterTurn(Monster monster, Level level, Player player) {
        // Получаем или создаем состояние ИИ для монстра
        MonsterAiState state = getOrCreateState(monster);

        Position monsterPos = new Position(monster.getX(), monster.getY());
        Position playerPos = new Position(player.getX(), player.getY());

        // Рассчитываем расстояние до игрока
        int distance = monsterPos.distance(playerPos);

        // Проверяем, видит ли монстр игрока (с учетом тумана войны)
        boolean canSeePlayer = canSeePlayer(monster, level, player);

        // Проверяем, должен ли монстр преследовать игрока
        boolean shouldChase = canSeePlayer && distance <= monster.getHostilityRange();

        MoveResult moveResult = null;

        if (shouldChase) {
            // Режим преследования - все монстры двигаются кратчайшим путем
            moveResult = chasePlayer(monster, level, player);
            state.setInCombat(true);

            // Специальная логика для призрака: в бою становится видимым
            if (monster.getMonsterType() == MonsterType.GHOST && monster.isInvisible()) {
                monster.setInvisible(false);
            }

            // Специальная логика для мимика: в бою раскрывается
            if (monster.getMonsterType() == MonsterType.MIMIC && monster.isInvisible()) {
                monster.setInvisible(false);
            }
        } else {
            // Режим патрулирования - каждый тип двигается по своему паттерну
            moveResult = patrol(monster, level, player);
            state.setInCombat(false);

            // Специальная логика для призрака: вне боя может стать невидимым
            if (monster.getMonsterType() == MonsterType.GHOST && !monster.isInvisible()) {
                if (random.nextDouble() < 0.2) { // 20% шанс стать невидимым
                    monster.setInvisible(true);
                }
            }

            // Специальная логика для мимика: вне боя притворяется предметом
            if (monster.getMonsterType() == MonsterType.MIMIC && !monster.isInvisible()) {
                if (random.nextDouble() < 0.1) { // 10% шанс замаскироваться
                    monster.setInvisible(true);
                }
            }
        }

        // Обрабатываем конец хода монстра (для огра и других)
        combatService.processMonsterTurnEnd(monster);
    }

    /** Преследование игрока по кратчайшему пути. */
    private MoveResult chasePlayer(Monster monster, Level level, Player player) {
        Position monsterPos = new Position(monster.getX(), monster.getY());
        Position playerPos = new Position(player.getX(), player.getY());

        // Пытаемся найти путь к игроку
        Direction nextStep =
                movementService.findPathStep(monsterPos, playerPos, level, monster.canMoveDiagonally());

        if (nextStep != null) {
            // Путь найден - двигаемся к игроку
            return movementService.resolveMonsterMovement(monster, nextStep, level, player);
        } else {
            // Путь не найден - двигаемся случайно
            return randomMovement(monster, level, player);
        }
    }

    /** Патрулирование по уникальному паттерну для типа монстра. */
    private MoveResult patrol(Monster monster, Level level, Player player) {
        MonsterType type = monster.getMonsterType();
        MonsterAiState state = getOrCreateState(monster);

        switch (type) {
            case ZOMBIE:
                return patrolZombie(monster, level, player, state);
            case VAMPIRE:
                return patrolVampire(monster, level, player, state);
            case GHOST:
                return patrolGhost(monster, level, player, state);
            case OGRE:
                return patrolOgre(monster, level, player, state);
            case SNAKE_MAGE:
                return patrolSnakeMage(monster, level, player, state);
            case MIMIC:
                return patrolMimic(monster, level, player, state);
            default:
                return randomMovement(monster, level, player);
        }
    }

    /** Зомби: медленное случайное блуждание. Низкая ловкость, поэтому двигается реже и медленнее. */
    private MoveResult patrolZombie(
            Monster monster, Level level, Player player, MonsterAiState state) {
        // Зомби двигается не каждый ход (33% шанс)
        if (random.nextDouble() < 0.33) {
            return randomMovement(monster, level, player);
        }
        return new MoveResult(
                MoveResultType.MONSTER_BLOCKED,
                String.format("%s стоит на месте", monster.getMonsterType().name()),
                monster);
    }

    /** Вампир: патрулирует по периметру комнаты. */
    private MoveResult patrolVampire(
            Monster monster, Level level, Player player, MonsterAiState state) {
        Position currentPos = new Position(monster.getX(), monster.getY());

        // Получаем комнату, в которой находится вампир
        Room currentRoom = levelService.findRoomAt(level, currentPos);
        if (currentRoom == null) {
            return randomMovement(monster, level, player);
        }

        // Пробуем найти направление для движения (максимум 4 попытки)
        Direction startDir = state.getPatrolDirection();
        Direction currentDir = startDir;

        for (int attempt = 0; attempt < 4; attempt++) {
            if (currentDir == null) {
                currentDir = selectPerimeterDirection(currentRoom);
            }

            // Если достигли границы или нет направления, выбираем новое
            if (currentDir == null || isAtRoomBoundary(currentPos, currentRoom, currentDir)) {
                currentDir = selectPerimeterDirection(currentRoom);
                state.setPatrolDirection(currentDir);
                state.setStepsTaken(0);
            }

            // Проверяем, можно ли двигаться в выбранном направлении
            if (movementService.canMove(monster, currentDir, level, false)) {
                state.setPatrolDirection(currentDir);
                state.setStepsTaken(state.getStepsTaken() + 1);
                return movementService.resolveMonsterMovement(monster, currentDir, level, player);
            }

            // Пробуем следующее направление
            currentDir = currentDir.rotateClockwise();
        }

        // Если все направления заблокированы, просто стоим на месте
        return new MoveResult(
                MoveResultType.MONSTER_BLOCKED,
                String.format("%s не может двигаться", monster.getMonsterType().name()),
                monster);
    }

    /** Призрак: случайная телепортация по комнате + невидимость. */
    private MoveResult patrolGhost(
            Monster monster, Level level, Player player, MonsterAiState state) {
        // Призрак может телепортироваться (25% шанс)
        if (random.nextDouble() < 0.25) {
            Room currentRoom =
                    levelService.findRoomAt(level, new Position(monster.getX(), monster.getY()));
            if (currentRoom != null) {
                // Телепортируемся в случайную клетку комнаты
                Position newPos = levelService.getRandomFreePositionInRoom(level, currentRoom);
                if (newPos != null) {
                    // Перемещаем призрака без проверки пути
                    moveMonsterDirectly(monster, newPos, level);
                    monster.onTeleport(); // Обрабатываем эффекты телепортации
                    return new MoveResult(
                            MoveResultType.MONSTER_TELEPORTED,
                            String.format("%s телепортируется", monster.getMonsterType().name()),
                            monster);
                }
            }
        }

        // Иначе случайное движение
        return randomMovement(monster, level, player);
    }

    /** Огр: ходит на две клетки, потом останавливается. */
    private MoveResult patrolOgre(Monster monster, Level level, Player player, MonsterAiState state) {
        // Если огр отдыхает после атаки, пропускаем ход
        if (monster.isResting()) {
            monster.incrementTurnsSinceLastAttack();
            return new MoveResult(
                    MoveResultType.MONSTER_RESTING,
                    String.format("%s отдыхает", monster.getMonsterType().name()),
                    monster);
        }

        // Пробуем разные направления (максимум 4 попытки)
        for (int attempt = 0; attempt < 4; attempt++) {
            // Огр ходит на 2 клетки в одном направлении
            if (state.getPatrolDirection() == null || state.getStepsTaken() >= 2) {
                state.setPatrolDirection(Direction.randomCardinal());
                state.setStepsTaken(0);
            }

            Direction dir = state.getPatrolDirection();

            // Проверяем, можно ли сделать шаг
            if (movementService.canMove(monster, dir, level, false)) {
                state.setStepsTaken(state.getStepsTaken() + 1);
                return movementService.resolveMonsterMovement(monster, dir, level, player);
            } else {
                // Путь заблокирован, пробуем другое направление
                state.setPatrolDirection(dir.rotateClockwise());
                state.setStepsTaken(0);
                // continue - следующая итерация цикла
            }
        }

        // Если все 4 направления заблокированы, стоим на месте
        return new MoveResult(
                MoveResultType.MONSTER_BLOCKED,
                String.format("%s не может двигаться", monster.getMonsterType().name()),
                monster);
    }

    /** Змей-маг: ходит по диагонали, постоянно меняя направление. */
    private MoveResult patrolSnakeMage(
            Monster monster, Level level, Player player, MonsterAiState state) {

        // максимум 4 попытки (по диагоналям)
        for (int attempt = 0; attempt < 4; attempt++) {

            if (state.getPatrolDirection() == null || random.nextDouble() < 0.3) {
                state.setPatrolDirection(Direction.randomDiagonal());
            }

            Direction dir = state.getPatrolDirection();

            if (movementService.canMove(monster, dir, level, false)) {
                return movementService.resolveMonsterMovement(monster, dir, level, player);
            }

            // пробуем другое направление
            state.setPatrolDirection(dir.opposite());
        }

        // если вообще никуда нельзя идти — стоим
        return new MoveResult(MoveResultType.WAIT, "");
    }

    /**
     * Мимик: имитирует предмет - стоит на месте и невидим, пока игрок не подойдет. Когда игрок
     * приближается, становится видимым и преследует.
     */
    private MoveResult patrolMimic(
            Monster monster, Level level, Player player, MonsterAiState state) {
        Position monsterPos = new Position(monster.getX(), monster.getY());
        Position playerPos = new Position(player.getX(), player.getY());

        int distance = monsterPos.distance(playerPos);

        // Если мимик еще не раскрыт (невидим) и игрок далеко
        if (monster.isInvisible() && distance > monster.getHostilityRange()) {
            // Мимик притворяется предметом - стоит на месте
            return new MoveResult(
                    MoveResultType.MONSTER_BLOCKED,
                    String.format("%s замаскировался под предмет", monster.getMonsterType().name()),
                    monster);
        }

        // Если игрок подошел близко или мимик уже раскрыт
        if (monster.isInvisible()) {
            // Раскрываем мимика
            monster.setInvisible(false);
            return new MoveResult(MoveResultType.MONSTER_INVISIBLE, "Мимик раскрыл себя!", monster);
        }

        // Если мимик раскрыт, преследуем игрока
        return chasePlayer(monster, level, player);
    }

    /** Случайное движение (для всех монстров, когда нет пути). */
    private MoveResult randomMovement(Monster monster, Level level, Player player) {
        List<Direction> available =
                movementService.getAvailableDirections(monster, level, false, monster.canMoveDiagonally());

        if (available.isEmpty()) {
            return new MoveResult(
                    MoveResultType.MONSTER_BLOCKED,
                    String.format("%s не может двигаться", monster.getMonsterType().name()),
                    monster);
        }

        Direction randomDir = available.get(random.nextInt(available.size()));
        return movementService.resolveMonsterMovement(monster, randomDir, level, player);
    }

    /** Проверяет, видит ли монстр игрока (с учетом тумана войны). */
    private boolean canSeePlayer(Monster monster, Level level, Player player) {
        Tile playerTile = level.getTileMap().getTile(player.getX(), player.getY());

        // Монстр видит игрока только если клетка игрока видима (туман войны рассеян)
        // или если монстр в той же комнате
        return playerTile.isVisible() || areInSameRoom(monster, player, level);
    }

    /** Проверяют, находятся ли монстр и игрок в одной комнате. */
    private boolean areInSameRoom(Monster monster, Player player, Level level) {
        Position monsterPos = new Position(monster.getX(), monster.getY());
        Position playerPos = new Position(player.getX(), player.getY());

        Room monsterRoom = levelService.findRoomAt(level, monsterPos);
        Room playerRoom = levelService.findRoomAt(level, playerPos);

        return monsterRoom != null && monsterRoom.equals(playerRoom);
    }

    /** Проверяет, находится ли позиция на границе комнаты в заданном направлении. */
    private boolean isAtRoomBoundary(Position pos, Room room, Direction direction) {
        int nextX = pos.x() + direction.getDx();
        int nextY = pos.y() + direction.getDy();

        // Если следующий шаг выходит за пределы комнаты или упирается в стену
        return !room.containsInterior(nextX, nextY) || room.isWall(nextX, nextY);
    }

    /** Выбирает направление для патрулирования периметра комнаты. */
    private Direction selectPerimeterDirection(Room room) {
        Direction[] directions = Direction.cardinalDirections();
        return directions[random.nextInt(directions.length)];
    }

    /** Прямое перемещение монстра (для телепортации). */
    private void moveMonsterDirectly(Monster monster, Position newPos, Level level) {
        TileMap tileMap = level.getTileMap();

        // Убираем монстра со старой позиции
        tileMap.removeMonster(monster.getX(), monster.getY());

        // Устанавливаем на новую
        tileMap.setMonster(newPos.x(), newPos.y(), monster);
    }

    /** Получает или создает состояние ИИ для монстра. */
    private MonsterAiState getOrCreateState(Monster monster) {
        return aiStates.computeIfAbsent(monster, k -> new MonsterAiState());
    }

    /** Очищает состояния мертвых монстров. */
    private void cleanupDeadMonsters() {
        aiStates.entrySet().removeIf(entry -> !entry.getKey().isAlive());
    }
}
