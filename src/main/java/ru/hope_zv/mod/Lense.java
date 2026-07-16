package ru.hope_zv.mod;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import lombok.Getter;
import ru.hope_zv.mod.impl.commands.impl.LenseCommand;
import ru.hope_zv.mod.impl.config.LenseConfigManager;
import ru.hope_zv.mod.impl.config.LensePlayerConfigService;
import ru.hope_zv.mod.impl.hud.LenseHudController;
import ru.hope_zv.mod.impl.systems.PlayerTickSystem;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lense extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final Map<String, String> BLOCK_MOD_NAMES_MAP = new ConcurrentHashMap<>();
    public static final Map<String, String> MODEL_MOD_NAMES_MAP = new ConcurrentHashMap<>();

    @Getter
    private static volatile LenseHudController Controller;

    @Getter
    private static volatile Lense Instance;

    @Getter
    public final Config<LenseConfig> Config;

    @Getter
    private LenseConfigManager ConfigManager;

    @Getter
    private LensePlayerConfigService ConfigService;

    public Lense(@Nonnull JavaPluginInit init) {
        super(init);
        Instance = this;
        this.Config = this.withConfig(LenseConfig.CODEC);
    }

    private static void populateModNamesMap(Map<String, String> map, Set<String> keys, String modName) {
        if (keys == null) return;
        if (modName == null) modName = "";

        for (String key : keys) {
            map.put(key, modName);
        }
    }

    @Override
    protected void setup() {
        super.setup();

        ConfigManager = new LenseConfigManager(
                this.getDataDirectory(),
                this.Config::get
        );
        ConfigManager.load();

        ConfigService = new LensePlayerConfigService(ConfigManager);

        this.getEntityStoreRegistry().registerSystem(new PlayerTickSystem());
        this.getCommandRegistry().registerCommand(new LenseCommand(ConfigService));

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            PlayerRef playerRef = event.getPlayer().getPlayerRef();
            ConfigService.onPlayerConnect(playerRef);
        });

        this.getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            PlayerRef playerRef = event.getPlayerRef();
            ConfigService.onPlayerDisconnect(playerRef);

            if (Controller != null) {
                Controller.removeHud(playerRef);
            }
        });
    }

    @Override
    protected void start() {
        super.start();

        Controller = new LenseHudController();

        BLOCK_MOD_NAMES_MAP.clear();
        MODEL_MOD_NAMES_MAP.clear();

        BlockTypeAssetMap<String, BlockType> blockAssetMap = BlockType.getAssetMap();
        DefaultAssetMap<String, ModelAsset> modelAssetMap = ModelAsset.getAssetMap();

        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            String packName = pack.getName();
            String modName = pack.getManifest().getName();

            populateModNamesMap(BLOCK_MOD_NAMES_MAP, blockAssetMap.getKeysForPack(packName), modName);
            populateModNamesMap(MODEL_MOD_NAMES_MAP, modelAssetMap.getKeysForPack(packName), modName);
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        if (ConfigService != null) {
            ConfigService.onShutdown();
        }
    }

}
