package ru.hope_zv.mod.impl.context;

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
    @Nullable
    private BlockState blockState;
    private Vector3i targetPos;
    private Vector3i offsetPos;
    private float breakProgress;

    public BlockContext() {
        super();
    }

    public boolean update(Player player, PlayerRef playerRef, float dt, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        Vector3i targetBlockPos = TargetUtil.getTargetBlock(archetypeChunk.getReferenceTo(index), 8, commandBuffer);

        if (targetBlockPos != null) {
            World world = player.getWorld();
            if (world != null) {
                long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPos.x, targetBlockPos.z);
                WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
                if (chunk != null) {
                    BlockPosition pos = world.getBaseBlock(new BlockPosition(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z));

                    this.blockType = chunk.getBlockType(pos.x, pos.y, pos.z);
                    this.blockState = chunk.getState(pos.x, pos.y, pos.z);
                    this.targetPos = targetBlockPos;
                    this.offsetPos = new Vector3i(pos.x, pos.y, pos.z);

                    this.breakProgress = 0;
                    ChunkStore chunkStore = world.getChunkStore();
                    Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
                    Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);

                    if (chunkReference != null) {
                        BlockHealthChunk blockHealthComponent = chunkStoreStore.getComponent(chunkReference, BlockHealthModule.get().getBlockHealthChunkComponentType());
                        if (blockHealthComponent != null) {
                            float health = blockHealthComponent.getBlockHealth(targetBlockPos);
                            this.breakProgress = 1 - health;
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public void clear() {
        this.blockType = null;
        this.blockState = null;
        this.targetPos = null;
        this.offsetPos = null;
    }
}