package com.triassicisland.security;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A security keycard. Opens readers that need this level or lower. */
public class KeycardItem extends Item {
    public final int level;

    public KeycardItem(int level, Properties properties) {
        super(properties);
        this.level = level;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.keycard", level).withStyle(ChatFormatting.GRAY));
    }
}
