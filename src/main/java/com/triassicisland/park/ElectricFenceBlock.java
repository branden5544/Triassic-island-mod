package com.triassicisland.park;

import com.triassicisland.power.ElectricalBlock;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Electric fence panel. Connects like iron bars (and to fence gates), conducts power to the fence blocks it
 * touches, and shocks whatever touches it while energized. Stack it for taller fences.
 */
public class ElectricFenceBlock extends IronBarsBlock implements EntityBlock, ElectricalBlock {
    /** Set by the block entity when the fence is live, so the client can show sparks. */
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");

    public final FenceTier tier;

    public ElectricFenceBlock(FenceTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(ENERGIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENERGIZED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(dir));
            if (attachesToGate(neighbor, dir)) state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, dir, neighbor, level, pos, neighborPos);
        if (attachesToGate(neighbor, dir)) updated = updated.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
        return updated;
    }

    private static boolean attachesToGate(BlockState neighbor, Direction dir) {
        if (neighbor.getBlock() instanceof FencePillarBlock) return dir.getAxis().isHorizontal();
        return neighbor.getBlock() instanceof ElectricFenceGateBlock && dir.getAxis().isHorizontal()
                && neighbor.getValue(ElectricFenceGateBlock.FACING).getClockWise().getAxis() == dir.getAxis();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFenceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ELECTRIC_FENCE.get()) return null;
        return (lvl, pos, st, be) -> ((ElectricFenceBlockEntity) be).serverTick();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ENERGIZED) && random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}
