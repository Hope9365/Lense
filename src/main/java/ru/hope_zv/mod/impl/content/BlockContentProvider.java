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
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;
import ru.hope_zv.mod.api.content.ContentProvider;
import ru.hope_zv.mod.impl.context.BlockContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class BlockContentProvider implements ContentProvider<BlockContext> {

    // Limit matches the statically defined UI slots (10 per row, 6 rows) to avoid missing element IDs.
    private static final int MAX_CONTAINER_ITEMS = 60;

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

    private static String formatCompactQuantity(int quantity) {
        if (quantity < 1000) {
            return Integer.toString(quantity);
        }

        if (quantity < 1_000_000) {
            return formatWithUnit(quantity, 1_000, "k");
        }

        if (quantity < 1_000_000_000) {
            return formatWithUnit(quantity, 1_000_000, "M");
        }

        return formatWithUnit(quantity, 1_000_000_000, "B");
    }

    private static String formatWithUnit(int quantity, int unit, String suffix) {
        double value = (double) quantity / unit;
        if (value < 10) {
            double rounded = Math.round(value * 10.0) / 10.0;
            if (rounded == Math.rint(rounded)) {
                return (long) rounded + suffix;
            }
            return String.format(Locale.US, "%.1f%s", rounded, suffix);
        }
        return Math.round(value) + suffix;
    }

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
                    Message farmingGrowthLabelMessage = growthPercentDisplay >= 100 ?
                            Message.translation("server.lense.hud.farming_fully_grown").color(DESC_COLOR) :
                            Message.translation("server.lense.hud.farming_growth_percent").param("percent", growthPercentDisplay).color(DESC_COLOR);
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
                            deferredBuilder.set("#LenseProcessingBenchTierLabel.TextSpans", Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR));
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
                            deferredBuilder.set("#LenseBenchTierLabel.TextSpans", Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR));
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

                        deferredBuilder.set("#LenseItemContainerState.Visible", true);
                        if (!stacks.isEmpty()) {
                            deferredBuilder.set("#LenseContainerItems.Visible", true);
                        }

                        for (int i = 1; i <= MAX_CONTAINER_ITEMS; i++) {
                            deferredBuilder.set("#LenseContainerItem" + i + ".Visible", false);
                        }

                        int renderCount = Math.min(stacks.size(), MAX_CONTAINER_ITEMS);
                        for (int i = 0; i < renderCount; i++) {
                            int slot = i + 1;
                            ItemStack stack = stacks.get(i);
                            ItemStack iconStack = stack.withQuantity(1);
                            deferredBuilder.set("#LenseContainerItem" + slot + ".Visible", true);
                            deferredBuilder.set("#LenseContainerItem" + slot + "Grid.ItemStacks", List.of(iconStack));
                            deferredBuilder.set("#LenseContainerItem" + slot + "Quantity.TextSpans", Message.raw(formatCompactQuantity(stack.getQuantity())));
                        }

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
