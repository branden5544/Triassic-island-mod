package com.triassicisland.power;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Electrical Age's energy meter: counts the energy that flows through it. */
public class EnergyMeterBlockEntity extends ElectricalBlockEntity {
    private double joules;
    private double watts;

    public EnergyMeterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_METER.get(), pos, state);
    }

    @Override
    public void onLinkCurrent(double amps) {
        watts = Math.abs(voltage) * amps;
        joules += watts / 20;
        setChanged();
    }

    void reset() {
        joules = 0;
        setChanged();
    }

    private double kwh() {
        return joules / 3_600_000;
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  %.3f kWh total", kwh()));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("energy_kwh", args -> kwh());
        methods.put("watts", args -> watts);
        methods.put("reset", args -> {
            reset();
            return null;
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Joules", joules);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        joules = tag.getDouble("Joules");
    }
}
