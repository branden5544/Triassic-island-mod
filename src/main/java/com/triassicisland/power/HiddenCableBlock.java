package com.triassicisland.power;

import com.triassicisland.network.DataBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A full-block cable that can look like any other block. Right-click it with a block to disguise it;
 * sneak + right-click with an empty hand to remove the disguise. Engineer's Goggles show the cable underneath.
 * A lever placed on it works as a switch: lever off, no power through this block.
 */
public class HiddenCableBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock, CableCarrier {
    @Nullable
    private final CableTier tier;
    private final boolean data;

    public HiddenCableBlock(@Nullable CableTier tier, boolean data, Properties properties) {
        super(properties);
        this.tier = tier;
        this.data = data;
    }

    @Nullable
    @Override
    public CableTier powerTier() {
        return tier;
    }

    @Override
    public boolean carriesData() {
        return data;
    }

    @Override
    public boolean connectsOn(BlockState state, Direction face) {
        return tier != null;
    }

    @Override
    public boolean connectsData(BlockState state, Direction face) {
        return data;
    }

    @Override
    public boolean isDataCable() {
        return data;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HiddenCableBlockEntity cable)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (cable.disguise() == null) return InteractionResult.PASS;
            if (!level.isClientSide) cable.setDisguise(null);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        // Only disguise a bare cable, so you can still build against disguised ones.
        if (cable.disguise() != null || !(held.getItem() instanceof BlockItem blockItem)) return InteractionResult.PASS;
        if (blockItem.getBlock() instanceof EntityBlock) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.triassicisland.disguise.invalid"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            BlockState disguise = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, held, hit));
            if (disguise == null) disguise = blockItem.getBlock().defaultBlockState();
            if (disguise.getRenderShape() != RenderShape.MODEL) {
                player.displayClientMessage(Component.translatable("message.triassicisland.disguise.invalid"), true);
            } else {
                cable.setDisguise(disguise);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HiddenCableBlockEntity cable) cable.updateSwitch();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HiddenCableBlockEntity(pos, state);
    }
}
