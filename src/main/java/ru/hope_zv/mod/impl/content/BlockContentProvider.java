package ru.hope_zv.mod.impl.content;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchTierLevel;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;
import ru.hope_zv.mod.api.content.ContentProvider;
import ru.hope_zv.mod.impl.context.BlockContext;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("removal")
public class BlockContentProvider implements ContentProvider<BlockContext> {

    private static final ThreadLocal<NumberFormat> QUANTITY_FORMAT = ThreadLocal.withInitial(() -> {
        NumberFormat f = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        f.setMaximumFractionDigits(1);
        f.setMinimumFractionDigits(0);
        f.setRoundingMode(RoundingMode.DOWN);
        return f;
    });
    private int allocatedContainerSlots = 0;
    private int lastRenderedContainerSlots = 0;

    private static Message getDisplayName(BlockType type) {
        Item item = type.getItem();
        if (item != null) {
            ItemTranslationProperties translations = item.getTranslationProperties();
            if (translations != null) {
                String nameKey = translations.getName();
                if (nameKey != null) {
                    return Message.translation(nameKey);
                }
            }
        }

        return Message.raw(type.getId());
    }

    public static String formatCompactQuantity(long number) {
        return QUANTITY_FORMAT.get().format(number);
    }

    private static String buildSlotMarkup(int n) {
        return (
                "Group #LenseContainerItem" + n + " {"
                        + " Visible: false;"
                        + " LayoutMode: Top;"
                        + " Anchor: (Width: 76 * 0.45, Height: 110 * 0.45);"
                        + " ItemGrid #LenseContainerItem" + n + "Grid {"
                        + "   Anchor: (Width: 74 * 0.45, Height: 74 * 0.45, Left: 1 * 0.45);"
                        + "   SlotsPerRow: 1;"
                        + "   RenderItemQualityBackground: true;"
                        + "   InfoDisplay: None;"
                        + "   ShowScrollbar: false;"
                        + "   AreItemsDraggable: false;"
                        + " }"
                        + " Group { Anchor: (Height: 0.9); }"
                        + " Label #LenseContainerItem" + n + "Quantity {"
                        + "   Anchor: (Height: 16 * 0.45, Left: 0, Right: 0);"
                        + "   Style: LabelStyle(FontSize: 14, TextColor: #FFFFFF, HorizontalAlignment: Center);"
                        + " }"
                        + "}"
        );
    }

    public void resetUiState() {
        allocatedContainerSlots = 0;
        lastRenderedContainerSlots = 0;
    }

    private void ensureContainerSlots(DeferredUICommandBuilder deferredBuilder, int neededSlots) {
        if (neededSlots <= allocatedContainerSlots) return;

        for (int i = allocatedContainerSlots + 1; i <= neededSlots; i++) {
            deferredBuilder.appendInline("#LenseContainerItems", buildSlotMarkup(i));

            deferredBuilder.set(
                    "#LenseContainerItem" + i + "Grid.Style",
                    Value.ref("Hud/Lense/Elements/States/ItemContainerState.ui", "LenseItemSlotStyle")
            );
        }

        allocatedContainerSlots = neededSlots;
    }

    @Override
    public void updateContent(BlockContext context, DeferredUICommandBuilder deferredBuilder) {
        BlockType blockType = context.getBlockType();
        if (!blockType.getId().equals("Empty")) {
            deferredBuilder.set("#LenseInfoHeader.Visible", true);
            deferredBuilder.set("#LenseInfoHeader.TextSpans", getDisplayName(blockType));

            Item item = blockType.getItem();
            if (item != null) {
                deferredBuilder.set("#LenseIconContainer.Visible", true);
                deferredBuilder.set("#LenseIcon.ItemId", item.getId());
            }

            float breakProgress = context.getBreakProgress();
            if (breakProgress >= 0) {
                deferredBuilder.set("#LenseBlockBreakProgressContainer.Visible", true);
                deferredBuilder.set("#LenseBlockBreakProgress.Value", context.getBreakProgress());
            }

            // Components

            // Farming
            FarmingData farmingData = blockType.getFarming();
            if (farmingData != null) {
                int growthPercentDisplay = -1;

                FarmingBlock farmingBlock = context.getFarmingBlock();
                if (farmingBlock != null) {
                    Map<String, FarmingStageData[]> dataStages = farmingData.getStages();
                    if (dataStages != null && !dataStages.isEmpty()) {

                        String stageSet = farmingBlock.getCurrentStageSet();
                        FarmingStageData[] stages = stageSet != null ? dataStages.get(stageSet) : null;

                        if (stages == null) {
                            String starting = farmingData.getStartingStageSet();
                            stages = starting != null ? dataStages.get(starting) : null;
                        }
                        if (stages == null) {
                            stages = dataStages.values().iterator().next();
                        }

                        int stageCount = (stages != null ? stages.length : 0);

                        if (stageCount <= 1) {
                            growthPercentDisplay = 100;
                        } else {
                            float maxGrowthStages = stageCount - 1;
                            float growthProgress = farmingBlock.getGrowthProgress();
                            float clamped = Math.clamp(growthProgress, 0, maxGrowthStages);
                            float growthPercent = clamped / maxGrowthStages;
                            growthPercentDisplay = Math.round(growthPercent * 100);
                        }
                    }
                } else {
                    growthPercentDisplay = 100;
                }

                deferredBuilder.set("#LenseFarmingComponent.Visible", true);
                if (growthPercentDisplay != -1) {
                    deferredBuilder.set("#LenseFarmingGrowthLabel.Visible", true);
                    Message farmingGrowthLabelMessage = growthPercentDisplay >= 100
                            ? Message.translation("server.lense.hud.farming_fully_grown").color(DESC_COLOR)
                            : Message.translation("server.lense.hud.farming_growth_percent")
                            .param("percent", growthPercentDisplay)
                            .color(DESC_COLOR);

                    deferredBuilder.set("#LenseFarmingGrowthLabel.TextSpans", farmingGrowthLabelMessage);
                }
            }
            //

            //

            // States
            if (context.getBlockState() != null) {
                BlockState state = context.getBlockState();
                switch (state) {
                    case ProcessingBenchState processor: {
                        int tier = processor.getTierLevel();
                        float progress = -1;

                        if (processor.isActive() && processor.getRecipe() != null) {
                            float inputProgress = processor.getInputProgress();
                            float recipeTime = processor.getRecipe().getTimeSeconds();

                            if (recipeTime > 0) {
                                BenchTierLevel levelData = processor.getBench().getTierLevel(tier);
                                float timeReduction = levelData != null ? levelData.getCraftingTimeReductionModifier() : 0;
                                if (timeReduction > 0) {
                                    recipeTime -= recipeTime * timeReduction;
                                }

                                progress = inputProgress / recipeTime;
                            }
                        }

                        deferredBuilder.set("#LenseProcessingBenchState.Visible", true);
                        if (tier != -1) {
                            deferredBuilder.set(
                                    "#LenseProcessingBenchTierLabel.TextSpans",
                                    Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR)
                            );
                        }
                        if (progress != -1) {
                            deferredBuilder.set("#LenseProcessingBenchProgress.Visible", true);
                            deferredBuilder.set("#LenseProcessingBenchProgressBar.Value", Math.clamp(progress, 0, 1));
                        }
                        break;
                    }

                    case BenchState bench: {
                        int tier = bench.getTierLevel();

                        deferredBuilder.set("#LenseBenchState.Visible", true);
                        if (tier != -1) {
                            deferredBuilder.set(
                                    "#LenseBenchTierLabel.TextSpans",
                                    Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR)
                            );
                        }
                        break;
                    }

                    case ItemContainerState container: {
                        List<ItemStack> stacks = new ArrayList<>();

                        r:
                        for (short i = 0; i < container.getItemContainer().getCapacity(); ++i) {
                            ItemStack stack = container.getItemContainer().getItemStack(i);
                            if (stack != null) {
                                for (int j = 0; j < stacks.size(); ++j) {
                                    ItemStack itemStack = stacks.get(j);
                                    if (itemStack.isEquivalentType(stack)) {
                                        stacks.set(j, itemStack.withQuantity(itemStack.getQuantity() + stack.getQuantity()));
                                        continue r;
                                    }
                                }
                                stacks.add(stack);
                            }
                        }

                        if (stacks.isEmpty()) {
                            break;
                        }

                        deferredBuilder.set("#LenseItemContainerState.Visible", true);
                        deferredBuilder.set("#LenseContainerItems.Visible", true);

                        int renderCount = stacks.size();

                        int perRow = Math.min(renderCount, 9);
                        int width = Math.round((76 * 0.45f) * perRow);

                        Anchor anchor = new Anchor();
                        anchor.setWidth(Value.of(width));

                        deferredBuilder.setObject("#LenseContainerItems.Anchor", anchor);

                        ensureContainerSlots(deferredBuilder, renderCount);

                        for (int i = 0; i < renderCount; i++) {
                            int slot = i + 1;
                            ItemStack stack = stacks.get(i);
                            ItemStack iconStack = stack.withQuantity(1);
                            if (iconStack == null) {
                                iconStack = stack;
                            }

                            deferredBuilder.set("#LenseContainerItem" + slot + ".Visible", true);
                            deferredBuilder.set("#LenseContainerItem" + slot + "Grid.ItemStacks", List.of(iconStack));
                            deferredBuilder.set(
                                    "#LenseContainerItem" + slot + "Quantity.TextSpans",
                                    Message.raw(formatCompactQuantity(stack.getQuantity()))
                            );
                        }

                        if (lastRenderedContainerSlots > renderCount) {
                            for (int slot = renderCount + 1; slot <= lastRenderedContainerSlots; slot++) {
                                deferredBuilder.set("#LenseContainerItem" + slot + ".Visible", false);
                            }
                        }

                        lastRenderedContainerSlots = renderCount;
                        break;
                    }

                    default: {
                        break;
                    }
                }
            }
            //

            String modName = Lense.BLOCK_MOD_NAMES_MAP.get(blockType.getId());
            if (modName != null && !modName.isBlank()) {
                deferredBuilder.set("#LenseInfoFooter.Visible", true);
                deferredBuilder.set("#LenseInfoFooter.TextSpans", Message.raw(modName).color(MOD_NAME_COLOR).bold(true));
            }
        }
    }
}
