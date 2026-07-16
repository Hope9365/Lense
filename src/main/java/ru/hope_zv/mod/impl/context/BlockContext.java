package ru.hope_zv.mod.impl.context;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionConfiguration;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import lombok.Getter;
import org.joml.Vector3i;
import ru.hope_zv.mod.api.context.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public class BlockContext extends Context {

    private static final float CREATIVE_DEFAULT_REACH = 10.0f;

    private BlockType blockType;
    private Vector3i targetPos;
    private Vector3i offsetPos;

    private float breakProgress;

    @Nullable
    private Ref<ChunkStore> chunkRef;
    @Nullable
    private Ref<ChunkStore> blockRef;

    @Nullable
    private FarmingBlock compFarmingBlock;
    @Nullable
    private Teleporter compTeleporter;
    @Nullable
    private BenchBlock compBench;
    @Nullable
    private ProcessingBenchBlock compProcessingBench;
    @Nullable
    private ItemContainerBlock compItemContainer;

    public BlockContext() {
        super();
    }

    public boolean update(Player player, PlayerRef playerRef, float dt, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        this.clear();

        Ref<EntityStore> selfRef = archetypeChunk.getReferenceTo(index);

        float reach = playerReach(player, selfRef, store);
        Vector3i targetBlockPos = TargetUtil.getTargetBlock(selfRef, reach, commandBuffer);
        if (targetBlockPos == null) return false;

        World world = player.getWorld();
        if (world == null) return false;

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

        long targetChunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPos.x, targetBlockPos.z);
        Ref<ChunkStore> targetChunkRef = chunkStore.getChunkReference(targetChunkIndex);
        if (targetChunkRef == null) return false;
        WorldChunk targetChunk = chunkStoreStore.getComponent(targetChunkRef, WorldChunk.getComponentType());
        if (targetChunk == null) return false;

        Vector3i basePos = resolveBaseBlock(targetChunk, targetBlockPos);

        long baseChunkIndex = ChunkUtil.indexChunkFromBlock(basePos.x, basePos.z);
        Ref<ChunkStore> baseChunkRef = baseChunkIndex == targetChunkIndex ? targetChunkRef : chunkStore.getChunkReference(baseChunkIndex);
        if (baseChunkRef == null) return false;
        WorldChunk baseChunk = baseChunkIndex == targetChunkIndex ? targetChunk : chunkStoreStore.getComponent(baseChunkRef, WorldChunk.getComponentType());
        if (baseChunk == null) return false;

        this.blockType = baseChunk.getBlockType(basePos.x, basePos.y, basePos.z);
        if (this.blockType == null) return false;

        this.targetPos = targetBlockPos;
        this.offsetPos = basePos;
        this.chunkRef = baseChunkRef;

        BlockHealthChunk blockHealthComponent =
                chunkStoreStore.getComponent(targetChunkRef, BlockHealthModule.get().getBlockHealthChunkComponentType());
        if (blockHealthComponent != null) {
            float health = blockHealthComponent.getBlockHealth(targetBlockPos);
            this.breakProgress = 1 - health;
        }

        BlockComponentChunk blockComponentChunk = chunkStoreStore.getComponent(baseChunkRef, BlockComponentChunk.getComponentType());
        if (blockComponentChunk != null) {
            int localX = basePos.x & 31;
            int localZ = basePos.z & 31;

            int blockIndex = ChunkUtil.indexBlockInColumn(localX, basePos.y, localZ);

            this.blockRef = blockComponentChunk.getEntityReference(blockIndex);

            if (this.blockRef != null) {
                this.compFarmingBlock = chunkStoreStore.getComponent(this.blockRef, FarmingBlock.getComponentType());
                this.compTeleporter = chunkStoreStore.getComponent(this.blockRef, Teleporter.getComponentType());
                this.compBench = chunkStoreStore.getComponent(this.blockRef, BenchBlock.getComponentType());
                this.compProcessingBench = chunkStoreStore.getComponent(this.blockRef, ProcessingBenchBlock.getComponentType());
                this.compItemContainer = chunkStoreStore.getComponent(this.blockRef, ItemContainerBlock.getComponentType());
            }
        }

        return true;
    }

    private static float playerReach(@Nonnull Player player, @Nonnull Ref<EntityStore> selfRef, @Nonnull Store<EntityStore> store) {
        GameMode gameMode = player.getGameMode();
        float reach = InteractionConfiguration.DEFAULT.getUseDistance(gameMode);
        if (gameMode == GameMode.Creative) {
            float creativeReach = CREATIVE_DEFAULT_REACH;
            PlayerSettings settings = store.getComponent(selfRef, PlayerSettings.getComponentType());
            if (settings != null) {
                creativeReach = Math.clamp((float) settings.creativeSettings().creativeInteractionDistance(), 0.0f, 128.0f);
            }
            reach = Math.max(reach, creativeReach);
        }
        return reach;
    }

    @Nonnull
    private static Vector3i resolveBaseBlock(@Nonnull WorldChunk chunk, @Nonnull Vector3i pos) {
        int filler = chunk.getFiller(pos.x, pos.y, pos.z);
        if (filler == 0) {
            return new Vector3i(pos);
        }
        return new Vector3i(
                pos.x - FillerBlockUtil.unpackX(filler),
                pos.y - FillerBlockUtil.unpackY(filler),
                pos.z - FillerBlockUtil.unpackZ(filler)
        );
    }

    public void clear() {
        this.blockType = null;
        this.targetPos = null;
        this.offsetPos = null;

        this.breakProgress = 0;

        this.chunkRef = null;
        this.blockRef = null;

        this.compFarmingBlock = null;
        this.compTeleporter = null;
        this.compBench = null;
        this.compProcessingBench = null;
        this.compItemContainer = null;
    }

}
