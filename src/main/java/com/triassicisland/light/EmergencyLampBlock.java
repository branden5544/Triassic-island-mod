package com.triassicisland.light;

import com.triassicisland.security.WallDeviceBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

/** An emergency lamp: charges its battery from the grid, and lights up from the battery when the power fails. */
public class EmergencyLampBlock extends WallDeviceBlock {
    public final double ratedVoltage;

    public EmergencyLampBlock(double ratedVoltage, Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LampSocketBlock.LIGHT)), 3, 3, 5, 11, EmergencyLampBlockEntity::new);
        this.ratedVoltage = ratedVoltage;
        registerDefaultState(defaultBlockState().setValue(LampSocketBlock.LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LampSocketBlock.LIGHT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof EmergencyLampBlockEntity lamp) lamp.serverTick();
        };
    }
}
