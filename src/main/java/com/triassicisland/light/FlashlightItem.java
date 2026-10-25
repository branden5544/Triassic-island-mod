package com.triassicisland.light;

import com.triassicisland.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A battery flashlight. Right-click to switch it on; while held it lights up whatever you point it at.
 * Recharge it in a Battery Charger, or by right-clicking a Battery block with it.
 */
public class FlashlightItem extends Item implements com.triassicisland.power.Chargeable {
    private static final String TAG_ON = "On", TAG_CHARGE = "Charge";

    public final int range;
    public final int brightness;
    /** Battery life in ticks. */
    public final int capacity;

    public FlashlightItem(int range, int brightness, int minutes, Properties properties) {
        super(properties.stacksTo(1));
        this.range = range;
        this.brightness = brightness;
        this.capacity = minutes * 1200;
    }

    public int charge(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains(TAG_CHARGE) ? tag.getInt(TAG_CHARGE) : capacity;
    }

    public void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt(TAG_CHARGE, Math.max(0, Math.min(capacity, charge)));
    }

    @Override
    public int getCharge(ItemStack stack) {
        return charge(stack);
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return capacity;
    }

    private static boolean isOn(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(TAG_ON);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean on = !isOn(stack) && charge(stack) > 0;
            stack.getOrCreateTag().putBoolean(TAG_ON, on);
            level.playSound(null, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.PLAYERS, 0.5f, on ? 1.4f : 1.1f);
            if (charge(stack) <= 0) player.displayClientMessage(Component.translatable("message.triassicisland.flashlight.empty"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player) || !isOn(stack)) return;
        boolean held = selected || player.getOffhandItem() == stack;
        if (!held) return;
        int charge = charge(stack);
        if (charge <= 0) {
            stack.getOrCreateTag().putBoolean(TAG_ON, false);
            return;
        }
        if (server.getGameTime() % 20 == 0) setCharge(stack, charge - 20);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1).scale(range));
        BlockHitResult hit = server.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        BlockPos target = hit.getType() == HitResult.Type.MISS ? BlockPos.containing(end) : hit.getBlockPos().relative(hit.getDirection());
        TemporaryLightBlock.light(server, target, brightness, 4, ModBlocks.TEMPORARY_LIGHT.get());
        // A little light around the player too, so the edges of the beam aren't pitch black.
        TemporaryLightBlock.light(server, player.blockPosition().above(), Math.max(1, brightness - 7), 4, ModBlocks.TEMPORARY_LIGHT.get());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return charge(stack) < capacity;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * charge(stack) / capacity);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isOn(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.flashlight", Math.round(100f * charge(stack) / capacity), range)
                .withStyle(ChatFormatting.GRAY));
    }
}
