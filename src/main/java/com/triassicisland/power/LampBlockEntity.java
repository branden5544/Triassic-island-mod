package com.triassicisland.power;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LampBlockEntity extends ElectricalBlockEntity {
    public LampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAMP.get(), pos, state);
    }

    private LampBlock lamp() {
        return (LampBlock) getBlockState().getBlock();
    }

    @Override
    protected double ratedVoltage() {
        return lamp().ratedVoltage;
    }

    @Override
    public double groundConductance() {
        return lamp().watts / (lamp().ratedVoltage * lamp().ratedVoltage);
    }

    void serverTick() {
        if (level == null || level.getGameTime() % 10 != 0) return;
        int light = (int) Math.round(15 * Math.min(1, Math.abs(power()) / lamp().watts));
        if (light < 2) light = 0;
        BlockState state = getBlockState();
        if (state.getValue(LampBlock.LIGHT) != light) level.setBlock(worldPosition, state.setValue(LampBlock.LIGHT, light), 3);
    }
}
