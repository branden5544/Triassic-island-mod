package com.triassicisland.light;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A light bulb. Remembers how much life it has left when taken out of a socket. */
public class BulbItem extends Item {
    private static final String TAG_LIFE = "Life";

    public final BulbType type;

    public BulbItem(BulbType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public static int life(ItemStack stack) {
        if (!(stack.getItem() instanceof BulbItem bulb)) return 0;
        var tag = stack.getTag();
        return tag != null && tag.contains(TAG_LIFE) ? tag.getInt(TAG_LIFE) : bulb.type.lifeTicks;
    }

    public static ItemStack withLife(ItemStack stack, int life) {
        stack.getOrCreateTag().putInt(TAG_LIFE, life);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return life(stack) < type.lifeTicks;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * life(stack) / type.lifeTicks);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFD040;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.bulb", (int) type.voltage, (int) type.watts,
                Math.round(100f * life(stack) / type.lifeTicks)).withStyle(ChatFormatting.GRAY));
        if (type.warmup > 0) tooltip.add(Component.translatable("tooltip.triassicisland.bulb.warmup").withStyle(ChatFormatting.DARK_GRAY));
        if (type.farming) tooltip.add(Component.translatable("tooltip.triassicisland.bulb.farming").withStyle(ChatFormatting.GREEN));
    }
}
