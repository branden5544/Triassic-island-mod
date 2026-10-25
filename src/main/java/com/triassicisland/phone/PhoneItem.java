package com.triassicisland.phone;

import com.triassicisland.power.Chargeable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A smartphone: message other players and, with Simple Voice Chat installed, call them. Needs WiFi (a powered router
 * within 48 blocks). Charge it in a Battery Charger.
 */
public class PhoneItem extends Item implements Chargeable {
    public static final int CAPACITY = 2000;
    private static final String TAG_CHARGE = "Charge";

    public PhoneItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getCharge(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains(TAG_CHARGE) ? tag.getInt(TAG_CHARGE) : CAPACITY;
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return CAPACITY;
    }

    @Override
    public void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt(TAG_CHARGE, Math.max(0, Math.min(CAPACITY, charge)));
    }

    /** Uses battery; returns false (and uses nothing) if there isn't enough. */
    public boolean use(ItemStack stack, int amount) {
        int charge = getCharge(stack);
        if (charge < amount) return false;
        setCharge(stack, charge - amount);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server) {
            if (getCharge(stack) <= 0) {
                player.displayClientMessage(Component.translatable("message.triassicisland.phone.flat").withStyle(ChatFormatting.RED), true);
            } else if (com.triassicisland.smart.Wifi.signal(server) == 0) {
                player.displayClientMessage(Component.translatable("message.triassicisland.phone.no_signal").withStyle(ChatFormatting.RED), true);
            } else {
                PhoneService.open(server);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) < CAPACITY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * getCharge(stack) / CAPACITY);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.phone", Math.round(100f * getCharge(stack) / CAPACITY)).withStyle(ChatFormatting.GRAY));
    }
}
