package ru.hope_zv.mod.impl.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.hud.HudAdapter;

import javax.annotation.Nonnull;

public class PlayerTickSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Query<EntityStore> query = Player.getComponentType();

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        HudAdapter hudAdapter = Lense.getAdapter();
        if (hudAdapter == null) {
            return;
        }
        Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (playerRef != null) {

            Player player = holder.getComponent(Player.getComponentType());
            if (player != null) {
                hudAdapter.showHud(player, playerRef, dt, index, archetypeChunk, store, commandBuffer);
            }

        }
    }

    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }

}
