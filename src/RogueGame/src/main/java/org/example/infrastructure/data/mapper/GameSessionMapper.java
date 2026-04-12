package org.example.infrastructure.data.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.example.domain.model.entity.*;
import org.example.domain.model.enums.*;
import org.example.infrastructure.data.dto.*;

/**
 * Маппер для преобразования GameSession между Entity и DTO. Отвечает за сохранение и восстановление
 * полного состояния игры.
 */
public class GameSessionMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Преобразует GameSession в GameSaveDto для сохранения. */
    public GameSaveDto toDto(GameSession session) {
        if (session == null) {
            return null;
        }

        GameSaveDto dto = new GameSaveDto();
        dto.setSessionId(session.getSessionId());
        dto.setCurrentLevelNumber(session.getCurrentLevelNumber());
        dto.setGameState(session.getGameState().name());

        dto.setPlayer(mapPlayerToDto(session.getPlayer()));
        dto.setLevel(mapLevelToDto(session.getCurrentLevel()));
        dto.setStatistics(mapStatisticsToDto(session.getStatistics()));

        return dto;
    }

    /** Восстанавливает GameSession из GameSaveDto. */
    public GameSession toEntity(GameSaveDto dto) {
        if (dto == null) {
            return null;
        }

        Player player = restorePlayer(dto.getPlayer());
        Level level = restoreLevel(dto.getLevel());
        GameStatistics statistics = restoreStatistics(dto.getStatistics());

        GameSession session =
                new GameSession(
                        dto.getSessionId(),
                        player,
                        level,
                        dto.getCurrentLevelNumber(),
                        GameState.valueOf(dto.getGameState()),
                        statistics);

        level.getTileMap().setPlayerPosition(player.getX(), player.getY());

        return session;
    }

    private PlayerDto mapPlayerToDto(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDto dto = new PlayerDto();
        dto.setX(player.getX());
        dto.setY(player.getY());
        dto.setHealth(player.getHealth());
        dto.setMaxHealth(player.getMaxHealth());
        dto.setAgility(player.getAgility());
        dto.setStrength(player.getStrength());
        dto.setAlive(player.isAlive());
        dto.setTurnsLived(player.getTurnsLived());

        if (player.getCurrentWeapon() != null) {
            dto.setCurrentWeapon(player.getCurrentWeapon().getSubType().name());
        }

        dto.setBackpack(mapBackpackToDto(player.getBackpack()));
        dto.setActiveEffects(mapEffectsToDto(player.getActiveEffects()));

        return dto;
    }

    private Player restorePlayer(PlayerDto dto) {
        if (dto == null) {
            return null;
        }

        Player player = new Player(dto.getX(), dto.getY());
        player.setHealth(dto.getHealth());
        player.setMaxHealth(dto.getMaxHealth());
        player.setAgility(dto.getAgility());
        player.setStrength(dto.getStrength());
        player.setAlive(dto.isAlive());
        player.setTurnsLived(dto.getTurnsLived());

        player.setBackpack(restoreBackpack(dto.getBackpack()));

        if (dto.getCurrentWeapon() != null && !dto.getCurrentWeapon().isEmpty()) {
            ItemSubType weaponType = ItemSubType.valueOf(dto.getCurrentWeapon());
            if (weaponType.isWeapon()) {
                player.setCurrentWeapon(new Weapon(weaponType));
            }
        }

        restoreActiveEffects(player, dto.getActiveEffects());

        return player;
    }

    private BackpackDto mapBackpackToDto(Backpack backpack) {
        if (backpack == null) {
            return null;
        }

        BackpackDto dto = new BackpackDto();
        dto.setTotalTreasureValue(backpack.getTotalTreasureValue());

        dto.setFood(
                backpack.getFood().stream()
                        .map(food -> food.getSubType().name())
                        .collect(Collectors.toList()));

        dto.setElixirs(
                backpack.getElixirs().stream()
                        .map(elixir -> elixir.getSubType().name())
                        .collect(Collectors.toList()));

        dto.setScrolls(
                backpack.getScrolls().stream()
                        .map(scroll -> scroll.getSubType().name())
                        .collect(Collectors.toList()));

        dto.setWeapons(
                backpack.getWeapons().stream()
                        .map(weapon -> weapon.getSubType().name())
                        .collect(Collectors.toList()));

        return dto;
    }

    private Backpack restoreBackpack(BackpackDto dto) {
        if (dto == null) {
            return new Backpack();
        }

        Backpack backpack = new Backpack();
        backpack.setTotalTreasureValue(dto.getTotalTreasureValue());

        // Восстанавливаем еду
        for (String foodType : dto.getFood()) {
            ItemSubType subType = ItemSubType.valueOf(foodType);
            if (subType.isFood()) {
                backpack.addItem(new Food(subType));
            }
        }

        // Восстанавливаем эликсиры
        for (String elixirType : dto.getElixirs()) {
            ItemSubType subType = ItemSubType.valueOf(elixirType);
            if (subType.isElixir()) {
                backpack.addItem(new Elixir(subType));
            }
        }

        // Восстанавливаем свитки
        for (String scrollType : dto.getScrolls()) {
            ItemSubType subType = ItemSubType.valueOf(scrollType);
            if (subType.isScroll()) {
                backpack.addItem(new Scroll(subType));
            }
        }

        // Восстанавливаем оружие
        for (String weaponType : dto.getWeapons()) {
            ItemSubType subType = ItemSubType.valueOf(weaponType);
            if (subType.isWeapon()) {
                backpack.addItem(new Weapon(subType));
            }
        }

        return backpack;
    }

    private List<TemporaryEffectDto> mapEffectsToDto(List<TemporaryEffect> effects) {
        if (effects == null) {
            return new ArrayList<>();
        }

        return effects.stream()
                .map(
                        effect ->
                                new TemporaryEffectDto(
                                        effect.id(),
                                        effect.stat().name(),
                                        effect.bonusValue(),
                                        effect.durationTurns(),
                                        effect.appliedAtTurn()))
                .collect(Collectors.toList());
    }

    private void restoreActiveEffects(Player player, List<TemporaryEffectDto> effectDtos) {
        if (effectDtos == null) {
            return;
        }

        for (TemporaryEffectDto dto : effectDtos) {
            TemporaryEffect effect =
                    new TemporaryEffect(
                            dto.getId(),
                            StatType.valueOf(dto.getStat()),
                            dto.getBonusValue(),
                            dto.getDurationTurns(),
                            dto.getAppliedAtTurn());
            player.addTemporaryEffect(effect);
            applyEffectToPlayer(player, effect);
        }
    }

    private void applyEffectToPlayer(Player player, TemporaryEffect effect) {
        switch (effect.stat()) {
            case STRENGTH:
                player.setStrength(player.getStrength() + effect.bonusValue());
                break;
            case AGILITY:
                player.setAgility(player.getAgility() + effect.bonusValue());
                break;
            case MAX_HEALTH:
                player.increaseMaxHealth(effect.bonusValue());
                break;
        }
    }

    private LevelDto mapLevelToDto(Level level) {
        if (level == null) {
            return null;
        }

        LevelDto dto = new LevelDto(level.getLevelNumber());

        // Комнаты
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Room room = level.getRoom(row, col);
                if (room != null) {
                    dto.addRoom(mapRoomToDto(room, row, col, level));
                }
            }
        }

        // Коридоры
        for (Corridor corridor : level.getCorridors()) {
            dto.addCorridor(mapCorridorToDto(corridor, level));
        }

        // Двери
        for (Door door : level.getDoors()) {
            dto.addDoor(mapDoorToDto(door));
        }

        // Выход
        Position exitPos = level.getExitTilePosition();
        if (exitPos != null) {
            dto.setExitTileX(exitPos.x());
            dto.setExitTileY(exitPos.y());
        }

        // Стартовая комната
        Position startPos = level.getStartRoomPosition();
        if (startPos != null) {
            dto.setStartRoomRow(startPos.x());
            dto.setStartRoomCol(startPos.y());
        }

        // Комната с выходом
        Position exitRoomPos = level.getExitRoomPosition();
        if (exitRoomPos != null) {
            dto.setExitRoomRow(exitRoomPos.x());
            dto.setExitRoomCol(exitRoomPos.y());
        }

        return dto;
    }

    private RoomDto mapRoomToDto(Room room, int row, int col, Level level) {
        if (room == null) {
            return null;
        }

        int roomId = row * 3 + col;
        RoomDto dto =
                new RoomDto(
                        roomId,
                        row,
                        col,
                        room.getTopLeft().x(),
                        room.getTopLeft().y(),
                        room.getWidth(),
                        room.getHeight());
        dto.setStartRoom(room.isStartRoom());
        dto.setExitRoom(room.isExitRoom());

        // Дверные проемы
        for (Position doorway : room.getDoorways()) {
            dto.addDoorway(doorway.x(), doorway.y());
        }

        // Монстры в комнате
        for (Position pos : room.getAllInteriorPositions()) {
            Monster monster = level.getMonsterAt(pos.x(), pos.y());
            if (monster != null && monster.isAlive()) {
                dto.addMonster(mapMonsterToDto(monster));
            }
        }

        // Предметы в комнате
        for (Position pos : room.getAllInteriorPositions()) {
            Item item = level.getItemAt(pos.x(), pos.y());
            if (item != null) {
                dto.addItem(mapItemToDto(item, pos));
            }
        }

        return dto;
    }

    private CorridorDto mapCorridorToDto(Corridor corridor, Level level) {
        if (corridor == null) {
            return null;
        }

        int fromRoomId = findRoomId(level, corridor.getRoomA());
        int toRoomId = findRoomId(level, corridor.getRoomB());

        CorridorDto dto = new CorridorDto(fromRoomId, toRoomId);
        dto.setType(corridor.getType().name());

        dto.setConnectionA(
                new PositionDto(corridor.getConnectionA().x(), corridor.getConnectionA().y()));
        dto.setConnectionB(
                new PositionDto(corridor.getConnectionB().x(), corridor.getConnectionB().y()));

        for (Position pos : corridor.getTiles()) {
            dto.addTile(pos.x(), pos.y());
        }

        return dto;
    }

    private DoorDto mapDoorToDto(Door door) {
        if (door == null) {
            return null;
        }

        return new DoorDto(door.getX(), door.getY(), door.getColor().name(), door.isOpen());
    }

    private int findRoomId(Level level, Room room) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (level.getRoom(row, col) == room) {
                    return row * 3 + col;
                }
            }
        }
        return -1;
    }

    private Room findRoomById(Level level, int roomId) {
        if (roomId < 0 || roomId >= 9) {
            return null;
        }
        int row = roomId / 3;
        int col = roomId % 3;
        return level.getRoom(row, col);
    }

    private Level restoreLevel(LevelDto dto) {
        if (dto == null) {
            return null;
        }

        Level level = new Level(dto.getLevelNumber());
        TileMap tileMap = level.getTileMap();

        // Шаг 1: Восстанавливаем комнаты
        for (RoomDto roomDto : dto.getRooms()) {
            Position topLeft = new Position(roomDto.getTopLeftX(), roomDto.getTopLeftY());
            Room room = new Room(topLeft, roomDto.getWidth(), roomDto.getHeight());
            room.setStartRoom(roomDto.isStartRoom());
            room.setExitRoom(roomDto.isExitRoom());

            level.setRoom(roomDto.getRow(), roomDto.getCol(), room);
            fillRoomOnTileMap(tileMap, room);
        }

        // Шаг 2: Восстанавливаем дверные проемы
        for (RoomDto roomDto : dto.getRooms()) {
            Room room = level.getRoom(roomDto.getRow(), roomDto.getCol());
            if (room != null) {
                for (PositionDto doorway : roomDto.getDoorways()) {
                    room.addDoorway(doorway.getX(), doorway.getY());
                    tileMap.setTileType(doorway.getX(), doorway.getY(), TileType.FLOOR);
                }
            }
        }

        // Шаг 3: Восстанавливаем коридоры
        for (CorridorDto corridorDto : dto.getCorridors()) {
            Room roomA = findRoomById(level, corridorDto.getFromRoomId());
            Room roomB = findRoomById(level, corridorDto.getToRoomId());

            if (roomA != null && roomB != null) {
                Position connectionA =
                        new Position(corridorDto.getConnectionA().getX(), corridorDto.getConnectionA().getY());
                Position connectionB =
                        new Position(corridorDto.getConnectionB().getX(), corridorDto.getConnectionB().getY());

                List<Position> tiles =
                        corridorDto.getTiles().stream()
                                .map(p -> new Position(p.getX(), p.getY()))
                                .collect(Collectors.toList());

                Corridor corridor = new Corridor(roomA, roomB, connectionA, connectionB, tiles);
                level.addCorridor(corridor);

                for (Position pos : tiles) {
                    tileMap.setTileType(pos.x(), pos.y(), TileType.CORRIDOR);
                }
            }
        }

        // Шаг 4: Восстанавливаем двери
        for (DoorDto doorDto : dto.getDoors()) {
            KeyColor color = KeyColor.valueOf(doorDto.getColor());
            Door door = new Door(doorDto.getX(), doorDto.getY(), color);
            if (doorDto.isOpen()) {
                door.open();
            }
            level.addDoor(door);
            tileMap.setTileType(doorDto.getX(), doorDto.getY(), TileType.DOOR);
        }

        // Шаг 5: Восстанавливаем монстров
        for (RoomDto roomDto : dto.getRooms()) {
            Room room = level.getRoom(roomDto.getRow(), roomDto.getCol());
            if (room != null) {
                for (MonsterDto monsterDto : roomDto.getMonsters()) {
                    Monster monster = restoreMonster(monsterDto);
                    if (monster != null) {
                        level.addMonster(monster);
                        tileMap.setMonster(monsterDto.getX(), monsterDto.getY(), monster);
                    }
                }
            }
        }

        // Шаг 6: Восстанавливаем предметы
        for (RoomDto roomDto : dto.getRooms()) {
            for (ItemDto itemDto : roomDto.getItems()) {
                Item item = restoreItem(itemDto);
                if (item != null) {
                    level.addItem(item, itemDto.getX(), itemDto.getY());
                }
            }
        }

        // Шаг 7: Восстанавливаем выход
        if (dto.getExitTileX() >= 0 && dto.getExitTileY() >= 0) {
            level.setExitTilePosition(new Position(dto.getExitTileX(), dto.getExitTileY()));
            tileMap.setTileType(dto.getExitTileX(), dto.getExitTileY(), TileType.EXIT);
        }

        // Шаг 8: Восстанавливаем позиции комнат
        if (dto.getStartRoomRow() >= 0 && dto.getStartRoomCol() >= 0) {
            level.setStartRoomPosition(new Position(dto.getStartRoomRow(), dto.getStartRoomCol()));
        }

        if (dto.getExitRoomRow() >= 0 && dto.getExitRoomCol() >= 0) {
            level.setExitRoomPosition(new Position(dto.getExitRoomRow(), dto.getExitRoomCol()));
        }

        return level;
    }

    private void fillRoomOnTileMap(TileMap tileMap, Room room) {
        Position topLeft = room.getTopLeft();
        for (int y = topLeft.y(); y < topLeft.y() + room.getHeight(); y++) {
            for (int x = topLeft.x(); x < topLeft.x() + room.getWidth(); x++) {
                if (room.containsInterior(x, y)) {
                    tileMap.setTileType(x, y, TileType.FLOOR);
                }
            }
        }
    }

    private MonsterDto mapMonsterToDto(Monster monster) {
        if (monster == null) {
            return null;
        }

        MonsterDto dto = new MonsterDto();
        dto.setX(monster.getX());
        dto.setY(monster.getY());
        dto.setType(monster.getMonsterType().name());
        dto.setHealth(monster.getHealth());
        dto.setMaxHealth(monster.getMaxHealth());
        dto.setAgility(monster.getAgility());
        dto.setStrength(monster.getStrength());
        dto.setAlive(monster.isAlive());
        dto.setResting(monster.isResting());
        dto.setInvisible(monster.isInvisible());
        dto.setFirstAttackDone(monster.isFirstAttackDone());
        dto.setTurnsSinceLastAttack(monster.getTurnsSinceLastAttack());

        return dto;
    }

    private Monster restoreMonster(MonsterDto dto) {
        if (dto == null) {
            return null;
        }

        MonsterType type = MonsterType.valueOf(dto.getType());
        Monster monster = new Monster(dto.getX(), dto.getY(), type);
        monster.setHealth(dto.getHealth());
        monster.setMaxHealth(dto.getMaxHealth());
        monster.setAgility(dto.getAgility());
        monster.setStrength(dto.getStrength());
        monster.setAlive(dto.isAlive());
        monster.setResting(dto.isResting());
        monster.setInvisible(dto.isInvisible());

        if (dto.isFirstAttackDone()) {
            monster.markFirstAttackDone();
        }

        for (int i = 0; i < dto.getTurnsSinceLastAttack(); i++) {
            monster.incrementTurnsSinceLastAttack();
        }

        return monster;
    }

    private ItemDto mapItemToDto(Item item, Position pos) {
        if (item == null) {
            return null;
        }

        return new ItemDto(
                item.getType().name(),
                item.getSubType().name(),
                item.getValue(),
                item.getName(),
                pos.x(),
                pos.y());
    }

    private Item restoreItem(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        ItemSubType subType = ItemSubType.valueOf(dto.getSubType());

        if (subType.isFood()) {
            return new Food(subType);
        } else if (subType.isElixir()) {
            return new Elixir(subType);
        } else if (subType.isScroll()) {
            return new Scroll(subType);
        } else if (subType.isWeapon()) {
            return new Weapon(subType);
        } else if (subType.isTreasure()) {
            return new Treasure(subType);
        } else if (subType.isKey()) {
            KeyColor color = subType.getKeyColor();
            return new Key(color);
        }

        return null;
    }

    private StatisticsDto mapStatisticsToDto(GameStatistics stats) {
        if (stats == null) {
            return null;
        }

        StatisticsDto dto = new StatisticsDto(stats.getSessionId());
        dto.setStartTime(stats.getStartTime().format(DATE_TIME_FORMATTER));
        dto.setEndTime(
                stats.getEndTime() != null ? stats.getEndTime().format(DATE_TIME_FORMATTER) : null);

        dto.setLevelReached(stats.getLevelReached());
        dto.setTotalTreasureCollected(stats.getTotalTreasureCollected());
        dto.setEnemiesDefeated(stats.getEnemiesDefeated());
        dto.setStepsTaken(stats.getStepsTaken());
        dto.setExplorationPercent(stats.getExplorationPercent());

        dto.setDamageDealt(stats.getDamageDealt());
        dto.setDamageTaken(stats.getDamageTaken());
        dto.setHitsDealt(stats.getHitsDealt());
        dto.setHitsTaken(stats.getHitsTaken());

        dto.setFoodEaten(stats.getFoodEaten());
        dto.setElixirsDrunk(stats.getElixirsDrunk());
        dto.setScrollsRead(stats.getScrollsRead());
        dto.setTotalHealing(stats.getTotalHealing());
        dto.setMaxHealthReached(stats.getMaxHealthReached());

        dto.setKillsByMonsterType(convertMonsterTypeMap(stats.getKillsByMonsterType()));
        dto.setFoodEatenByType(convertItemSubTypeMap(stats.getFoodEatenByType()));
        dto.setElixirsByType(convertItemSubTypeMap(stats.getElixirsByType()));
        dto.setScrollsByType(convertItemSubTypeMap(stats.getScrollsByType()));
        dto.setWeaponsUsed(convertItemSubTypeMap(stats.getWeaponsUsed()));
        dto.setItemsPickedUp(convertItemTypeMap(stats.getItemsPickedUp()));

        return dto;
    }

    private GameStatistics restoreStatistics(StatisticsDto dto) {
        if (dto == null) {
            return null;
        }

        GameStatistics stats = new GameStatistics(dto.getSessionId());
        stats.setStartTime(LocalDateTime.parse(dto.getStartTime(), DATE_TIME_FORMATTER));
        if (dto.getEndTime() != null) {
            stats.setEndTime(LocalDateTime.parse(dto.getEndTime(), DATE_TIME_FORMATTER));
        }

        stats.setLevelReached(dto.getLevelReached());
        stats.setTotalTreasureCollected(dto.getTotalTreasureCollected());
        stats.setEnemiesDefeated(dto.getEnemiesDefeated());
        stats.setStepsTaken(dto.getStepsTaken());
        stats.setExplorationPercent(dto.getExplorationPercent());

        stats.setDamageDealt(dto.getDamageDealt());
        stats.setDamageTaken(dto.getDamageTaken());
        stats.setHitsDealt(dto.getHitsDealt());
        stats.setHitsTaken(dto.getHitsTaken());

        stats.setFoodEaten(dto.getFoodEaten());
        stats.setElixirsDrunk(dto.getElixirsDrunk());
        stats.setScrollsRead(dto.getScrollsRead());
        stats.setTotalHealing(dto.getTotalHealing());
        stats.setMaxHealthReached(dto.getMaxHealthReached());

        restoreMonsterTypeMap(stats.getKillsByMonsterType(), dto.getKillsByMonsterType());
        restoreItemSubTypeMap(stats.getFoodEatenByType(), dto.getFoodEatenByType());
        restoreItemSubTypeMap(stats.getElixirsByType(), dto.getElixirsByType());
        restoreItemSubTypeMap(stats.getScrollsByType(), dto.getScrollsByType());
        restoreItemSubTypeMap(stats.getWeaponsUsed(), dto.getWeaponsUsed());
        restoreItemTypeMap(stats.getItemsPickedUp(), dto.getItemsPickedUp());

        return stats;
    }

    // Вспомогательные методы для маппинга Map
    private Map<String, Integer> convertMonsterTypeMap(Map<MonsterType, Integer> source) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<MonsterType, Integer> entry : source.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }
        return result;
    }

    private Map<String, Integer> convertItemSubTypeMap(Map<ItemSubType, Integer> source) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<ItemSubType, Integer> entry : source.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }
        return result;
    }

    private Map<String, Integer> convertItemTypeMap(Map<ItemType, Integer> source) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<ItemType, Integer> entry : source.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }
        return result;
    }

    private void restoreMonsterTypeMap(
            Map<MonsterType, Integer> target, Map<String, Integer> source) {
        if (source == null) return;
        target.clear();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            try {
                target.put(MonsterType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                // Игнорируем неизвестные типы
            }
        }
    }

    private void restoreItemSubTypeMap(
            Map<ItemSubType, Integer> target, Map<String, Integer> source) {
        if (source == null) return;
        target.clear();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            try {
                target.put(ItemSubType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                // Игнорируем неизвестные типы
            }
        }
    }

    private void restoreItemTypeMap(Map<ItemType, Integer> target, Map<String, Integer> source) {
        if (source == null) return;
        target.clear();
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            try {
                target.put(ItemType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                // Игнорируем неизвестные типы
            }
        }
    }
}
