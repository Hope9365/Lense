package ru.hope_zv.mod.impl.hud;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;
import ru.hope_zv.mod.api.content.ContentProvider;
import ru.hope_zv.mod.impl.content.BlockContentProvider;
import ru.hope_zv.mod.impl.content.EntityContentProvider;
import ru.hope_zv.mod.impl.context.BlockContext;
import ru.hope_zv.mod.impl.context.EntityContext;

import javax.annotation.Nonnull;

public class LenseHud extends CustomUIHud {

    private final BlockContentProvider blockContentProvider = new BlockContentProvider();
    private final ContentProvider<EntityContext> entityContentProvider = new EntityContentProvider();

    private final DeferredUICommandBuilder deferredBuilder = DeferredUICommandBuilder.create();
    private final BlockContext blockContext = new BlockContext();
    private final EntityContext entityContext = new EntityContext();

    public LenseHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    public void updateHud(
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        this.deferredBuilder.reset();

        boolean hudEnabled = Lense.getInstance().getConfigService().getConfig(playerRef).isHudEnabled();

        if (hudEnabled && player.getWorld() != null) {
            if (entityContext.update(player, dt, index, archetypeChunk, store, commandBuffer)) {
                entityContentProvider.updateContent(entityContext, deferredBuilder);
                blockContext.clear();
            } else if (blockContext.update(player, playerRef, dt, index, archetypeChunk, store, commandBuffer)) {
                blockContentProvider.updateContent(blockContext, deferredBuilder);
                entityContext.clear();
            }
        }

        if (hudEnabled && !deferredBuilder.getOperations().isEmpty()) {
            deferredBuilder.set("#LenseHud.Visible", true);
        }

    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        blockContentProvider.resetUiState();

        builder.append("Hud/Lense/Elements/Lense.ui");

        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/EntityHealth.ui");

        // Components
        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/Components/Farming.ui");
        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/Components/Teleporter.ui");

        // States
        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/ProcessingBenchState.ui");
        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/BenchState.ui");
        builder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/ItemContainerState.ui");


        deferredBuilder.applyTo(builder);
    }

}
