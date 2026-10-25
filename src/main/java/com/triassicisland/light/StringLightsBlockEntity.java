package com.triassicisland.light;

import com.triassicisland.power.CableBlockEntity;
import com.triassicisland.power.Voltage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StringLightsBlockEntity extends CableBlockEntity {
    private static final double WATTS = 6;
    private static final int MAX_LIGHT = 9;

    public StringLightsBlockEntity(BlockPos pos, BlockState state) {
        super(LightEntities.STRING_LIGHTS.get(), pos, state);
    }

    @Override
    public double groundConductance() {
        return WATTS / (Voltage.LOW * Voltage.LOW);
    }

    void serverTick() {
        if (level == null || level.getGameTime() % 10 != 0) return;
        double fraction = Math.min(1, Math.abs(power()) / WATTS);
        int light = fraction < 0.1 ? 0 : (int) Math.round(MAX_LIGHT * fraction);
        BlockState state = getBlockState();
        if (state.getValue(LampSocketBlock.LIGHT) != light) level.setBlock(worldPosition, state.setValue(LampSocketBlock.LIGHT, light), 3);
    }
}
