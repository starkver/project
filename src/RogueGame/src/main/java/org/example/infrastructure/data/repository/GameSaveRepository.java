package org.example.infrastructure.data.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import org.example.domain.model.entity.GameSession;
import org.example.infrastructure.data.dto.GameSaveDto;
import org.example.infrastructure.data.mapper.GameSessionMapper;

/** Репозиторий для сохранения и загрузки игровых сессий. */
public class GameSaveRepository {
    private static final String DATA_DIR = "data";
    private static final String SAVE_FILE = "data/save.json";

    private final Gson gson;
    private final GameSessionMapper mapper;

    public GameSaveRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.mapper = new GameSessionMapper();
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию сохранений: " + e.getMessage());
        }
    }

    // Сохранение игры (save.json)

    // Сохранить игровую сессию
    public void save(GameSession session) {
        if (session == null) {
            System.err.println("Нельзя сохранить null-сессию");
            return;
        }
        try {
            GameSaveDto dto = mapper.toDto(session);

            try (Writer writer = new FileWriter(SAVE_FILE)) {
                gson.toJson(dto, writer);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения игры: " + e.getMessage());
        }
    }

    // Загрузить игровую сессию
    public GameSession load() {
        try {
            Path path = Paths.get(SAVE_FILE);

            if (!Files.exists(path)) {
                return null;
            }

            try (Reader reader = new FileReader(SAVE_FILE)) {
                GameSaveDto dto = gson.fromJson(reader, GameSaveDto.class);
                return mapper.toEntity(dto);
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки игры: " + e.getMessage());
            return null;
        }
    }

    // Проверить, существует ли сохранение
    public boolean exists() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    // Удалить сохранение
    public void delete() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
        } catch (IOException e) {
            System.err.println("Ошибка удаления сохранения: " + e.getMessage());
        }
    }
}
