package com.triassicisland.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Holds a fuse in a cable run (front and back connect). Right-click with a fuse to insert, empty hand to take it out. */
public class FuseHolderBlock extends FacingElectricalBlock {
    public static final BooleanProperty HAS_FUSE = BooleanProperty.create("has_fuse");

    public FuseHolderBlock(Properties properties) {
        super(properties, FuseHolderBlockEntity::new);
        registerDefaultState(defaultBlockState().setValue(HAS_FUSE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_FUSE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FuseHolderBlockEntity holder)) return InteractionResult.PASS;
        var held = player.getItemInHand(hand);
        if (held.getItem() instanceof FuseItem fuse && holder.rating() == 0) {
            if (!level.isClientSide) {
                holder.insert(fuse.amps);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                var removed = holder.remove();
                if (!removed.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(removed);
                } else {
                    player.displayClientMessage(Component.translatable("message.triassicisland.fuse.empty"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
