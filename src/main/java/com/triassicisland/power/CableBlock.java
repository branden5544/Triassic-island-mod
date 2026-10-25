package com.triassicisland.power;

import com.triassicisland.network.DataBlock;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

/**
 * A visible cable. Power cables have a voltage tier; data cables have none; combined cables carry both.
 * Connects to neighbours that accept what it carries on the touching face.
 */
public class CableBlock extends PipeBlock implements EntityBlock, ElectricalBlock, DataBlock, CableCarrier {
    @Nullable
    private final CableTier tier;
    private final boolean data;

    public CableBlock(@Nullable CableTier tier, boolean data, Properties properties) {
        // Data-only cables are thinner.
        super(tier == null ? 0.125f : 0.1875f, properties);
        this.tier = tier;
        this.data = data;
        BlockState state = stateDefinition.any();
        for (Direction dir : Direction.values()) state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), false);
        registerDefaultState(state);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), CableCarrier.attaches(this, context.getLevel().getBlockState(context.getClickedPos().relative(dir)), dir));
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), CableCarrier.attaches(this, neighbor, dir));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return tier == null ? null : new CableBlockEntity(ModBlockEntities.CABLE.get(), pos, state);
    }
}
