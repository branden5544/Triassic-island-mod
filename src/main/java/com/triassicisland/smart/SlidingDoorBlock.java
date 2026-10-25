package com.triassicisland.smart;

import com.triassicisland.network.DataBlock;
import com.triassicisland.power.ElectricalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * An automatic sliding glass door, two blocks tall. The bottom half takes power (50 V) and data; it slides open when
 * someone comes near. Sneak + right-click cycles auto / held open / locked. The glass is drawn by a renderer so it can slide.
 */
public class SlidingDoorBlock extends DoorBlock implements EntityBlock, ElectricalBlock, DataBlock {
    private static final VoxelShape CLOSED_X = Block.box(0, 0, 7, 16, 16, 9), CLOSED_Z = Block.box(7, 0, 0, 9, 16, 16);
    private static final VoxelShape OPEN_X = Block.box(0, 0, 7, 2, 16, 9), OPEN_Z = Block.box(7, 0, 0, 9, 16, 2);

    public SlidingDoorBlock(Properties properties) {
        super(properties, BlockSetType.IRON);
    }

    /** The axis the glass spans (across the doorway). */
    public static Direction.Axis spanAxis(BlockState state) {
        return state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean alongX = spanAxis(state) == Direction.Axis.X;
        if (!state.getValue(OPEN)) return alongX ? CLOSED_X : CLOSED_Z;
        return alongX ? OPEN_X : OPEN_Z;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!(level.getBlockEntity(lower) instanceof SlidingDoorBlockEntity door)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                player.displayClientMessage(Component.translatable("message.triassicisland.door.mode." + door.cycleMode()), true);
            } else if (!door.isPowered()) {
                player.displayClientMessage(Component.translatable("message.triassicisland.no_power", 50,
                        String.format("%.0f", door.voltage())).withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Redstone doesn't work this door; it opens by itself. */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {}

    static void setOpenBoth(Level level, BlockPos lower, boolean open) {
        BlockState state = level.getBlockState(lower);
        if (!(state.getBlock() instanceof SlidingDoorBlock) || state.getValue(OPEN) == open) return;
        level.setBlock(lower, state.setValue(OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        BlockState upper = level.getBlockState(lower.above());
        if (upper.getBlock() instanceof SlidingDoorBlock) level.setBlock(lower.above(), upper.setValue(OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
    }

    /** Which way the glass slides along its span: toward the hinge side. */
    public static int slideDirection(BlockState state) {
        return state.getValue(HINGE) == DoorHingeSide.LEFT ? 1 : -1;
    }

    @Override
    public boolean connectsOn(BlockState state, Direction face) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public boolean connectsData(BlockState state, Direction face) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SlidingDoorBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        return (l, p, s, be) -> {
            if (be instanceof SlidingDoorBlockEntity door) {
                if (l.isClientSide) door.clientTick();
                else door.serverTick();
            }
        };
    }
}
