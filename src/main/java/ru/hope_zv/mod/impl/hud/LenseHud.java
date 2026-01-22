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

    private static final ContentProvider<BlockContext> BLOCK_CONTENT_PROVIDER = new BlockContentProvider();
    private static final ContentProvider<EntityContext> ENTITY_CONTENT_PROVIDER = new EntityContentProvider();

    private final DeferredUICommandBuilder deferredBuilder = DeferredUICommandBuilder.create();
    private final BlockContext blockContext = new BlockContext();
    private final EntityContext entityContext = new EntityContext();

    public LenseHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    public void updateHud(@Nonnull Player player, @Nonnull PlayerRef playerRef, float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        this.deferredBuilder.reset();

        boolean hudEnabled = Lense.getInstance().getConfigService().getConfig(playerRef).isHudEnabled();

        if (hudEnabled && player.getWorld() != null) {
            if (entityContext.update(player, dt, index, archetypeChunk, store, commandBuffer)) {
                ENTITY_CONTENT_PROVIDER.updateContent(entityContext, deferredBuilder);
                blockContext.clear();
            } else if (blockContext.update(player, playerRef, dt, index, archetypeChunk, store, commandBuffer)) {
                BLOCK_CONTENT_PROVIDER.updateContent(blockContext, deferredBuilder);
                entityContext.clear();
            }
        }

        UICommandBuilder builder = new UICommandBuilder();
        builder.remove("#LenseHud");
        builder.append("Hud/Lense/Elements/Lense.ui");
        if (hudEnabled && !deferredBuilder.getOperations().isEmpty()) {
            builder.set("#LenseHud.Visible", true);
            deferredBuilder.applyTo(builder);
        }
        this.update(false, builder);
        
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        builder.append("Hud/Lense/Elements/Lense.ui");

//        if (!deferredBuilder.getOperations().isEmpty()) {
//            builder.append("Hud/Lense/Elements/Lense.ui");
//            deferredBuilder.applyTo(builder);
//        }
//
//        this.update(false, builder);
    }

}