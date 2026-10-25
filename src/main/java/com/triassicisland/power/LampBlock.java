package com.triassicisland.power;

import com.triassicisland.network.DataBlock;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/** An electric lamp (Electrical Age light bulbs). Brightness follows the power it gets: dim on a sagging grid. */
public class LampBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock {
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    public final double ratedVoltage;
    public final double watts;

    public LampBlock(double ratedVoltage, double watts, Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIGHT)));
        this.ratedVoltage = ratedVoltage;
        this.watts = watts;
        registerDefaultState(stateDefinition.any().setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LampBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.LAMP.get(), (l, p, s, be) -> be.serverTick());
    }
}
