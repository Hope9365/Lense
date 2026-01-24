package ru.hope_zv.mod.impl.context;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import lombok.Getter;
import ru.hope_zv.mod.api.context.Context;

import javax.annotation.Nullable;

@Getter
public class BlockContext extends Context {

    private BlockType blockType;
    private Vector3i targetPos;
    private Vector3i offsetPos;

    private float breakProgress;

    @Nullable
    private Ref<ChunkStore> chunkRef;
    @Nullable
    private Ref<ChunkStore> blockRef;

    @Nullable
    private BlockState blockState;
    @Nullable
    private FarmingBlock farmingBlock;

    public BlockContext() {
        super();
    }

    public boolean update(Player player, PlayerRef playerRef, float dt, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        this.clear();

        Vector3i targetBlockPos = TargetUtil.getTargetBlock(archetypeChunk.getReferenceTo(index), 8, commandBuffer);
        if (targetBlockPos == null) return false;

        World world = player.getWorld();
        if (world == null) return false;

        BlockPosition blockPos = world.getBaseBlock(new BlockPosition(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z));

        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z);

        WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
        if (chunk == null) return false;

        this.blockType = chunk.getBlockType(blockPos.x, blockPos.y, blockPos.z);
        this.blockState = chunk.getState(blockPos.x, blockPos.y, blockPos.z);

        this.targetPos = targetBlockPos;
        this.offsetPos = new Vector3i(blockPos.x, blockPos.y, blockPos.z);

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

        this.chunkRef = chunkStore.getChunkReference(chunkIndex);

        if (this.chunkRef != null) {
            BlockHealthChunk blockHealthComponent =
                    chunkStoreStore.getComponent(this.chunkRef, BlockHealthModule.get().getBlockHealthChunkComponentType());
            if (blockHealthComponent != null) {
                float health = blockHealthComponent.getBlockHealth(targetBlockPos);
                this.breakProgress = 1 - health;
            }

            BlockComponentChunk blockComponentChunk = chunkStoreStore.getComponent(this.chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
                int localX = blockPos.x & 31;
                int localZ = blockPos.z & 31;

                int blockIndex = ChunkUtil.indexBlockInColumn(localX, blockPos.y, localZ);

                this.blockRef = blockComponentChunk.getEntityReference(blockIndex);

                if (this.blockRef != null) {
                    this.farmingBlock = chunkStoreStore.getComponent(this.blockRef, FarmingBlock.getComponentType());
                }
            }

        }

        return true;
    }

    public void clear() {
        this.blockType = null;
        this.targetPos = null;
        this.offsetPos = null;

        this.breakProgress = 0;

        this.chunkRef = null;
        this.blockRef = null;

        this.blockState = null;
        this.farmingBlock = null;
    }

}