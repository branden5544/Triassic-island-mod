package com.triassicisland.park;

import com.triassicisland.network.DataBlock;
import com.triassicisland.power.ElectricalBlock;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Electric fence gate (rated 800 V). Opens with redstone, or by hand - but grabbing it while it's energized
 * gets you shocked, so cut the power with a breaker first. Only passes power along the fence while closed.
 */
public class ElectricFenceGateBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ENERGIZED = ElectricFenceBlock.ENERGIZED;
    private static final VoxelShape CLOSED_X = Block.box(0, 0, 7, 16, 16, 9);
    private static final VoxelShape CLOSED_Z = Block.box(7, 0, 0, 9, 16, 16);
    private static final VoxelShape OPEN_X = Shapes.or(Block.box(0, 0, 7, 2, 16, 9), Block.box(14, 0, 7, 16, 16, 9));
    private static final VoxelShape OPEN_Z = Shapes.or(Block.box(7, 0, 0, 9, 16, 2), Block.box(7, 0, 14, 9, 16, 16));

    public ElectricFenceGateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(POWERED, false).setValue(ENERGIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, ENERGIZED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(OPEN, powered).setValue(POWERED, powered);
    }

    /** The gate spans across the direction the player faced when placing it, so it connects to the fence on its two ends. */
    @Override
    public boolean connectsOn(BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean alongX = state.getValue(FACING).getAxis() == Direction.Axis.Z;
        if (state.getValue(OPEN)) return alongX ? OPEN_X : OPEN_Z;
        return alongX ? CLOSED_X : CLOSED_Z;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElectricFenceGateBlockEntity gate) {
            if (gate.isEnergized()) {
                gate.shock(player);
                player.displayClientMessage(Component.translatable("message.triassicisland.gate.energized"), true);
            } else {
                setOpen(level, pos, state, !state.getValue(OPEN));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) setOpen(level, pos, state.setValue(POWERED, powered), powered);
    }

    static void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        level.setBlockAndUpdate(pos, state.setValue(OPEN, open));
        level.playSound(null, pos, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1, 0.8f);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFenceGateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ELECTRIC_FENCE_GATE.get()) return null;
        return (lvl, pos, st, be) -> ((ElectricFenceGateBlockEntity) be).serverTick();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ENERGIZED) && !state.getValue(OPEN) && random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}
