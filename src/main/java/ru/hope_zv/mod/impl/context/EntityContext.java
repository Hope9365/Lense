package ru.hope_zv.mod.impl.context;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import lombok.Getter;
import ru.hope_zv.mod.api.context.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@Getter
public class EntityContext extends Context {
    private static final Set<String> MODELS_BLACKLIST = Set.of(
            "NPC_Path_Marker",
            "NPC_Spawn_Marker",
            "Objective_Location_Marker"
    );

    private Store<EntityStore> entityStore;
    private Ref<EntityStore> entity;
    @Nullable
    private String modelAssetId;

    public EntityContext() {
        super();
    }

    public boolean update(@Nonnull Player player, float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        this.clear();
        
        Ref<EntityStore> targetEntity = TargetUtil.getTargetEntity(archetypeChunk.getReferenceTo(index), commandBuffer);

        if (targetEntity != null) {
            ModelComponent modelComponent = store.getComponent(targetEntity, ModelComponent.getComponentType());
            if (modelComponent != null) {
                String assetId = modelComponent.getModel().getModelAssetId();

                if (MODELS_BLACKLIST.contains(assetId)) {
                    return false;
                }

                this.entityStore = store;
                this.entity = targetEntity;
                this.modelAssetId = assetId;

                return true;
            }
        }

        return false;
    }

    public void clear() {
        this.entityStore = null;
        this.entity = null;
        this.modelAssetId = null;
    }
}