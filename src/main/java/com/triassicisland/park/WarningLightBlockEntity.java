package com.triassicisland.park;

import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WarningLightBlockEntity extends ElectricalBlockEntity {
    private static final double WATTS = 5;

    public WarningLightBlockEntity(BlockPos pos, BlockState state) {
        super(ParkEntities.WARNING_LIGHT.get(), pos, state);
    }

    /** A small load at whatever voltage the fence runs (sized for 200 V; fine up to 800 V). */
    @Override
    public double groundConductance() {
        return WATTS / (200.0 * 200.0);
    }

    void serverTick() {
        if (level == null || level.getGameTime() % 10 != 0) return;
        boolean live = Math.abs(voltage) >= ElectricShock.MIN_VOLTS;
        boolean lit = live && (level.getGameTime() / 10) % 2 == 0;
        BlockState state = getBlockState();
        if (state.getValue(WarningLightBlock.LIT) != lit) level.setBlock(worldPosition, state.setValue(WarningLightBlock.LIT, lit), 3);
    }
}
