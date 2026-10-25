package com.triassicisland.power;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A 200 V wind turbine. Wind is measured at the rotor on top of the tower. It gets stronger with height
 * (about a third at sea level, full strength 64 blocks above it), picks up in rain and storms, and gusts slowly
 * over time. Needs open sky above the rotor. Up to 1.5 kW in a storm.
 */
public class WindTurbineBlockEntity extends ElectricalBlockEntity {
    private static final double MAX_WATTS = 1000;

    private double wind;

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIND_TURBINE.get(), pos, state);
    }

    private void updateWind() {
        wind = windAt(level, getBlockPos());
    }

    /** Wind strength (0-1.5) for a turbine based at {@code base}. Works on both sides, so the renderer can spin the rotor. */
    public static double windAt(net.minecraft.world.level.Level level, BlockPos base) {
        if (level == null || !level.dimensionType().hasSkyLight()) return 0;
        BlockPos hub = base.above(WindTurbineBlock.HEIGHT - 1);
        if (!level.canSeeSky(hub.above())) return 0;
        // A third of full wind at ground level, full wind 64 blocks above sea level.
        double height = 0.35 + 0.65 * Math.max(0, Math.min(1, (hub.getY() - level.getSeaLevel()) / 64.0));
        double weather = level.isThundering() ? 1.5 : level.isRaining() ? 1.2 : 0.8;
        double gust = 0.85 + 0.15 * Math.sin(level.getGameTime() / 600.0 + base.asLong() % 1000);
        return height * weather * gust;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(getBlockPos()).inflate(3, 0, 3).expandTowards(0, WindTurbineBlock.HEIGHT + 2, 0);
    }

    /** Short-circuit current, chosen so the most power it can deliver (at half voltage) is wind x MAX_WATTS. */
    private double shortCircuitAmps() {
        return 4 * MAX_WATTS * wind / Voltage.MEDIUM;
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.MEDIUM;
    }

    @Override
    public double groundConductance() {
        updateWind();
        return shortCircuitAmps() / Voltage.MEDIUM;
    }

    @Override
    public double sourceCurrent() {
        return shortCircuitAmps();
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  wind %.0f%%", wind * 100));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("wind", args -> wind * 100);
    }
}
