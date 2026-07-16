package ru.hope_zv.mod.impl.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.impl.hud.LenseHudController;

import javax.annotation.Nonnull;

public class PlayerTickSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query = Player.getComponentType();

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        LenseHudController controller = Lense.getController();
        if (controller == null) {
            return;
        }
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef != null) {

            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                controller.showHud(player, playerRef, dt, index, archetypeChunk, store, commandBuffer);
            }

        }
    }

    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }

}
