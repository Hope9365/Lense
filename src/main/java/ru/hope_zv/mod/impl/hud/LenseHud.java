package ru.hope_zv.mod.impl.hud;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LenseHud extends CustomUIHud {

    private static final int BASE_TOP = 52;
    private static final float SLIDE_OFFSET = 1f;
    private static final float APPEAR_DURATION = 0.12f;
    private static final float DISAPPEAR_DURATION = 0.12f;
    private static final float BREAK_LERP_SPEED = 12f;
    private static final float BREAK_SNAP_EPSILON = 0.004f;

    private final BlockContentProvider blockContentProvider = new BlockContentProvider();
    private final ContentProvider<EntityContext> entityContentProvider = new EntityContentProvider();

    private final DeferredUICommandBuilder deferredBuilder = DeferredUICommandBuilder.create();
    private final BlockContext blockContext = new BlockContext();
    private final EntityContext entityContext = new EntityContext();

    private String lastTargetKey;
    private float appear;
    private float shownBreak;
    private List<Consumer<UICommandBuilder>> lastContentOps;

    private boolean hasLastHash = false;
    private int lastHash = 0;

    public LenseHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, LenseHudController.HUD_KEY);
    }

    private static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        float f = -2f * t + 2f;
        return 1f - (f * f * f) / 2f;
    }

    public boolean updateHud(
            @Nonnull Player player,
            @Nonnull PlayerRef playerRef,
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        this.deferredBuilder.reset();
        blockContentProvider.resetUiState();

        boolean hudEnabled = Lense.getInstance().getConfigService().getConfig(playerRef).isHudEnabled();

        String targetKey = null;
        boolean blockActive = false;
        float breakTarget = -1f;

        if (hudEnabled && player.getWorld() != null) {
            if (entityContext.update(player, dt, index, archetypeChunk, store, commandBuffer)) {
                entityContentProvider.updateContent(entityContext, deferredBuilder);
                blockContext.clear();
                targetKey = "e:" + entityContext.getModelAssetId();
            } else if (blockContext.update(player, playerRef, dt, index, archetypeChunk, store, commandBuffer)) {
                blockContentProvider.updateContent(blockContext, deferredBuilder);
                entityContext.clear();
                blockActive = true;
                targetKey = "b:" + blockContext.getBlockType().getId();
                breakTarget = blockContext.getBreakProgress();
            }
        }

        boolean hasTarget = targetKey != null && !deferredBuilder.getOperations().isEmpty();

        if (hasTarget) {
            lastContentOps = new ArrayList<>(deferredBuilder.getOperations());
            if (!targetKey.equals(lastTargetKey)) {
                appear = 0f;
            }
            appear = Math.min(1f, appear + (APPEAR_DURATION <= 0f ? 1f : dt / APPEAR_DURATION));
        } else {
            appear = Math.max(0f, appear - (DISAPPEAR_DURATION <= 0f ? 1f : dt / DISAPPEAR_DURATION));
            if (appear > 0f && lastContentOps != null) {
                for (Consumer<UICommandBuilder> op : lastContentOps) {
                    deferredBuilder.custom(op);
                }
            }
        }
        lastTargetKey = targetKey;

        boolean visible = hasTarget || appear > 0f;

        if (visible) {
            float eased = easeInOutCubic(appear);
            int top = Math.round(BASE_TOP + (1f - eased) * SLIDE_OFFSET);
            Anchor anchor = new Anchor();
            anchor.setTop(Value.of(top));
            deferredBuilder.setObject("#LenseHudInner.Anchor", anchor);

            if (blockActive && breakTarget >= 0f) {
                if (Math.abs(breakTarget - shownBreak) < BREAK_SNAP_EPSILON) {
                    shownBreak = breakTarget;
                } else {
                    shownBreak += (breakTarget - shownBreak) * Math.clamp(dt * BREAK_LERP_SPEED, 0f, 1f);
                }
                deferredBuilder.set("#LenseBlockBreakProgress.Value", Math.clamp(shownBreak, 0f, 1f));
            }
        } else {
            shownBreak = 0f;
            lastContentOps = null;
        }

        deferredBuilder.set("#LenseHud.Visible", visible);

        int hash = deferredBuilder.computeOperationCommandsHash();
        boolean isDirty = !hasLastHash || hash != lastHash;
        hasLastHash = true;
        lastHash = hash;

        return isDirty;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
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
