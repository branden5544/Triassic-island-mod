package com.triassicisland.dna;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** An item carrying a species (and optionally a DNA quality): cleaned fossils, DNA samples, dinosaur eggs. */
public class GeneticItem extends Item {
    private static final String TAG_INCUBATED = "Incubated";

    private final boolean showQuality;

    public GeneticItem(Properties properties, boolean showQuality) {
        super(properties);
        this.showQuality = showQuality;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Species.of(stack)
                .<Component>map(s -> Component.translatable(getDescriptionId(stack) + ".of", s.displayName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Species.of(stack).isEmpty()) {
            tooltip.add(Component.translatable("tooltip.triassicisland.unknown_species").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (showQuality) {
            int quality = Species.quality(stack);
            ChatFormatting color = quality >= 75 ? ChatFormatting.GREEN : quality >= 40 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            tooltip.add(Component.translatable("tooltip.triassicisland.quality", quality).withStyle(color));
        }
        if (isIncubated(stack)) {
            tooltip.add(Component.translatable("tooltip.triassicisland.incubated").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isIncubated(stack);
    }

    public static boolean isIncubated(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(TAG_INCUBATED);
    }

    public static ItemStack markIncubated(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TAG_INCUBATED, true);
        return stack;
    }
}
