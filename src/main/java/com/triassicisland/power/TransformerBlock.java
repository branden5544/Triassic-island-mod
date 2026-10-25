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

/**
 * Back face = input, front face = output. Right-click to switch between stepping up and down.
 * The variable transformer also changes its ratio with sneak + right-click.
 */
public class TransformerBlock extends FacingElectricalBlock {
    public static final BooleanProperty STEP_UP = BooleanProperty.create("step_up");

    public TransformerBlock(Properties properties) {
        this(properties, TransformerBlockEntity::new);
    }

    protected TransformerBlock(Properties properties, net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier<? extends net.minecraft.world.level.block.entity.BlockEntity> factory) {
        super(properties, factory);
        registerDefaultState(defaultBlockState().setValue(STEP_UP, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STEP_UP);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof VariableTransformerBlockEntity variable) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.triassicisland.transformer.ratio", variable.cycleRatio()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            BlockState next = state.cycle(STEP_UP);
            level.setBlockAndUpdate(pos, next);
            player.displayClientMessage(Component.translatable(next.getValue(STEP_UP)
                    ? "message.triassicisland.transformer.step_up" : "message.triassicisland.transformer.step_down"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
