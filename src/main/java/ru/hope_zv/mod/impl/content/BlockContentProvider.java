package ru.hope_zv.mod.impl.content;

import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchTierLevel;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import ru.hope_zv.mod.Lense;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;
import ru.hope_zv.mod.api.content.ContentProvider;
import ru.hope_zv.mod.impl.context.BlockContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BlockContentProvider implements ContentProvider<BlockContext> {

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

                        deferredBuilder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/ProcessingBenchState.ui");
                        if (tier != -1) {
                            deferredBuilder.set("#LenseTierLabel.TextSpans", Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR));
                        }
                        if (progress != -1) {
                            deferredBuilder.set("#LenseProcessingProgress.Visible", true);
                            deferredBuilder.set("#LenseProcessingProgressBar.Value", Math.clamp(progress, 0, 1));
                        }

                        break;
                    }
                    case BenchState bench: {
                        int tier = -1;

                        tier = bench.getTierLevel();

                        deferredBuilder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/BenchState.ui");
                        if (tier != -1) {
                            deferredBuilder.set("#LenseTierLabel.TextSpans", Message.translation("server.lense.hud.tier").param("tier", tier).color(DESC_COLOR));
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

                        deferredBuilder.append("#LenseInfoBodyInner", "Hud/Lense/Elements/States/ItemContainerState.ui");
                        if (!stacks.isEmpty()) {
                            deferredBuilder.set("#LenseContainerItemGrid.ItemStacks", stacks);
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

            String modName = Lense.BLOCK_MOD_NAMES_MAP.get(blockType.getId());
            if (modName != null && !modName.isBlank()) {
                deferredBuilder.set("#LenseInfoFooter.Visible", true);
                deferredBuilder.set("#LenseInfoFooter.TextSpans", Message.raw(modName).color(MOD_NAME_COLOR).bold(true));
            }

        }
    }

}
