package ru.hope_zv.mod.impl.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.LenseConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class LenseConfigManager {

    private static final Type CONFIG_MAP_TYPE = new TypeToken<HashMap<String, LensePlayerConfig>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configFile;
    private final Supplier<LenseConfig> serverConfigSupplier;

    private final Map<UUID, LensePlayerConfig> configs = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public LenseConfigManager(
            @Nonnull Path dataDirectory,
            @Nonnull Supplier<LenseConfig> serverConfigSupplier
    ) {
        this.configFile = dataDirectory.resolve("per_player_cfg.json");
        this.serverConfigSupplier = serverConfigSupplier;

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            Lense.LOGGER.atSevere().log("Failed to create directory: %s", dataDirectory);
        }
    }

    @Nullable
    private static UUID parseUuid(String str) {
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void load() {
        if (!Files.exists(configFile)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            Map<String, LensePlayerConfig> loaded = gson.fromJson(reader, CONFIG_MAP_TYPE);
            if (loaded == null) {
                return;
            }

            configs.clear();
            for (Map.Entry<String, LensePlayerConfig> entry : loaded.entrySet()) {
                UUID uuid = parseUuid(entry.getKey());
                if (uuid != null && entry.getValue() != null) {
                    configs.put(uuid, entry.getValue());
                }
            }

            Lense.LOGGER.atInfo().log("Loaded %d player configs", configs.size());

        } catch (Exception e) {
            Lense.LOGGER.atSevere().log("Failed to load player configs: %s", e.getMessage());
        }
    }

    public void save() {
        if (!dirty.get() || configs.isEmpty()) {
            return;
        }
        writeToFile();
    }

    public void forceSave() {
        if (configs.isEmpty()) {
            return;
        }
        writeToFile();
    }

    private void writeToFile() {
        Map<String, LensePlayerConfig> toSave = new HashMap<>();
        for (Map.Entry<UUID, LensePlayerConfig> entry : configs.entrySet()) {
            toSave.put(entry.getKey().toString(), entry.getValue());
        }

        Path tempFile = configFile.resolveSibling(configFile.getFileName() + ".tmp");

        try {
            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                gson.toJson(toSave, writer);
            }

            Files.move(tempFile, configFile, StandardCopyOption.REPLACE_EXISTING);
            dirty.set(false);
            
            Lense.LOGGER.atInfo().log("Saved %d player configs", configs.size());

        } catch (IOException e) {
            Lense.LOGGER.atSevere().log("Failed to save player configs: %s", e.getMessage());
        }
    }

    @Nonnull
    public LensePlayerConfig getOrCreate(@Nonnull UUID uuid) {
        LensePlayerConfig existing = configs.get(uuid);
        if (existing != null) {
            return existing;
        }

        LensePlayerConfig newConfig = LensePlayerConfig.createWithDefaults(serverConfigSupplier.get());
        LensePlayerConfig previous = configs.putIfAbsent(uuid, newConfig);

        if (previous != null) {
            return previous;
        }

        dirty.set(true);
        return newConfig;
    }

    public void markDirty() {
        dirty.set(true);
    }
}
