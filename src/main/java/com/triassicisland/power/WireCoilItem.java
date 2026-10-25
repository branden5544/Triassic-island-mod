package com.triassicisland.power;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Strings an overhead wire: right-click one Pole Insulator, then another (up to 32 blocks away).
 * Uses one coil per 16 blocks of wire. Right-click the air to cancel.
 */
public class WireCoilItem extends Item {
    private static final String TAG_START = "WireStart";

    public final CableTier tier;

    public WireCoilItem(CableTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!(level.getBlockEntity(pos) instanceof PoleInsulatorBlockEntity here)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_START)) {
            if (!here.hasRoom()) return message(player, "message.triassicisland.wire.full");
            stack.getOrCreateTag().put(TAG_START, NbtUtils.writeBlockPos(pos));
            return message(player, "message.triassicisland.wire.start");
        }
        BlockPos start = NbtUtils.readBlockPos(tag.getCompound(TAG_START));
        stack.removeTagKey(TAG_START);
        if (start.equals(pos)) return message(player, "message.triassicisland.wire.cancelled");
        if (!(level.getBlockEntity(start) instanceof PoleInsulatorBlockEntity there)) return message(player, "message.triassicisland.wire.gone");
        double length = PoleInsulatorBlockEntity.length(start, pos);
        if (length > PoleInsulatorBlockEntity.MAX_LENGTH) return message(player, "message.triassicisland.wire.too_far");
        if (here.isConnectedTo(start)) return message(player, "message.triassicisland.wire.already");
        if (!here.hasRoom() || !there.hasRoom()) return message(player, "message.triassicisland.wire.full");
        int coils = PoleInsulatorBlockEntity.coilsFor(length);
        if (player != null && !player.getAbilities().instabuild) {
            if (stack.getCount() < coils) {
                player.displayClientMessage(Component.translatable("message.triassicisland.wire.need", coils), true);
                return InteractionResult.FAIL;
            }
            stack.shrink(coils);
        }
        here.addWire(start, tier);
        there.addWire(pos, tier);
        if (player != null) player.displayClientMessage(Component.translatable("message.triassicisland.wire.done", String.format("%.0f", length)), true);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult message(@Nullable Player player, String key) {
        if (player != null) player.displayClientMessage(Component.translatable(key), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getTag() != null && stack.getTag().contains(TAG_START)) {
            stack.removeTagKey(TAG_START);
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.triassicisland.wire.cancelled"), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(TAG_START);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.triassicisland.wire_coil", (int) tier.ratedVoltage).withStyle(ChatFormatting.GRAY));
    }
}
