package com.triassicisland.power;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A small 50 V solar panel. Behaves like a current source: up to 4 A in full sun, less at dawn, dusk
 * and in rain, nothing at night or under a roof. Pair it with a battery to store daytime power.
 */
public class SolarPanelBlockEntity extends ElectricalBlockEntity {
    private static final double MAX_AMPS = 4;

    private double sun;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    private void updateSun() {
        sun = 0;
        if (level == null || !level.dimensionType().hasSkyLight() || !level.canSeeSky(getBlockPos().above())) return;
        sun = Math.max(0, Math.cos(level.getSunAngle(1)));
        if (level.isRaining()) sun *= 0.4;
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.LOW;
    }

    @Override
    public double groundConductance() {
        updateSun();
        return MAX_AMPS * sun / Voltage.LOW;
    }

    @Override
    public double sourceCurrent() {
        return MAX_AMPS * sun;
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  sunlight %.0f%%", sun * 100));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("sunlight", args -> sun * 100);
    }
}
