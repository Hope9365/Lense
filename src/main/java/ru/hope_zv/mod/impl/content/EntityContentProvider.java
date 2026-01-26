package ru.hope_zv.mod.impl.content;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;
import ru.hope_zv.mod.api.content.ContentProvider;
import ru.hope_zv.mod.impl.context.EntityContext;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class EntityContentProvider implements ContentProvider<EntityContext> {

    private static final ThreadLocal<DecimalFormat> STAT_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,###.#", symbols);
    });


    private static String formatStatValue(float value) {
        if (!Float.isFinite(value)) {
            return String.valueOf(value);
        }
        return STAT_FORMAT.get().format(value);
    }

    public void updateContent(EntityContext context, DeferredUICommandBuilder deferredBuilder) {
        Store<EntityStore> store = context.getEntityStore();
        Ref<EntityStore> entity = context.getEntity();
        String modelAssetId = context.getModelAssetId();

        DisplayNameComponent displayName = store.getComponent(entity, DisplayNameComponent.getComponentType());
        if (displayName != null) {
            deferredBuilder.set("#LenseInfoHeader.Visible", true);
            deferredBuilder.set("#LenseInfoHeader.TextSpans", displayName.getDisplayName());
        }

        EntityStatMap stats = store.getComponent(entity, EntityStatMap.getComponentType());
        if (stats != null) {
            int statIndex = EntityStatType.getAssetMap().getIndex("Health");
            EntityStatValue entityStatValue = stats.get(statIndex);
            if (entityStatValue != null) {
                deferredBuilder.set("#LenseEntityHealthContainer.Visible", true);
                deferredBuilder.set("#LenseHealthLabel.TextSpans", Message.raw(" %s/%s".formatted(formatStatValue(entityStatValue.get()), formatStatValue(entityStatValue.getMax()))));
            }
        }

        if (modelAssetId != null) {
            String modName = Lense.MODEL_MOD_NAMES_MAP.get(modelAssetId);
            if (modName != null && !modName.isBlank()) {
                deferredBuilder.set("#LenseInfoFooter.Visible", true);
                deferredBuilder.set("#LenseInfoFooter.TextSpans", Message.raw(modName).color(MOD_NAME_COLOR).bold(true));
            }
        }

    }

}
