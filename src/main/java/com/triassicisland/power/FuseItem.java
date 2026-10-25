package com.triassicisland.power;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A lead fuse for a Fuse Holder. Blows (and is used up) when the current goes over its rating. */
public class FuseItem extends Item {
    public final int amps;

    public FuseItem(int amps, Properties properties) {
        super(properties);
        this.amps = amps;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.fuse", amps).withStyle(ChatFormatting.GRAY));
    }
}
