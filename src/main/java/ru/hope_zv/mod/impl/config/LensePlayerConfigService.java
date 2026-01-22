package ru.hope_zv.mod.impl.config;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.UUID;

public class LensePlayerConfigService {

    private final LenseConfigManager configManager;

    public LensePlayerConfigService(@Nonnull LenseConfigManager configManager) {
        this.configManager = configManager;
    }

    @Nonnull
    public LensePlayerConfig getConfig(@Nonnull PlayerRef playerRef) {
        return configManager.getOrCreate(playerRef.getUuid());
    }

    @Nonnull
    public LensePlayerConfig getConfig(@Nonnull UUID uuid) {
        return configManager.getOrCreate(uuid);
    }

    public void markDirty() {
        configManager.markDirty();
    }

    public void onPlayerConnect(@Nonnull PlayerRef playerRef) {
        configManager.getOrCreate(playerRef.getUuid());
    }

    public void onPlayerDisconnect(@Nonnull PlayerRef playerRef) {
        configManager.save();
    }
    
    public void onSave() {
        configManager.save();
    }

    public void onShutdown() {
        configManager.forceSave();
    }
}