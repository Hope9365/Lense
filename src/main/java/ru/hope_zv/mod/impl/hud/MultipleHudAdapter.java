package ru.hope_zv.mod.impl.hud;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import ru.hope_zv.mod.api.hud.HudAdapter;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MultipleHudAdapter implements HudAdapter {

    private final Map<UUID, LenseHud> huds = new ConcurrentHashMap<>();

    @Override
    public void showHud(@Nonnull Player player, @Nonnull PlayerRef playerRef, float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        LenseHud hud = huds.computeIfAbsent(playerRef.getUuid(), _ -> new LenseHud(playerRef));

        boolean isDirty = hud.updateHud(player, playerRef, dt, index, archetypeChunk, store, commandBuffer);
        if (isDirty) {
            MultipleHUD.getInstance().setCustomHud(player, playerRef, "LenseHud", hud);
        }

    }

    @Override
    public void removeHud(@Nonnull PlayerRef playerRef) {
        huds.remove(playerRef.getUuid());

//        Ref<EntityStore> ref = playerRef.getReference();
//        if (ref == null || !ref.isValid()) return;
//
//        Store<EntityStore> store = ref.getStore();
//        Player player = store.getComponent(ref, Player.getComponentType());
//
//        if (player != null) {
//            MultipleHUD.getInstance().hideCustomHud(player, playerRef, "LenseHud");
//        }
    }

}
