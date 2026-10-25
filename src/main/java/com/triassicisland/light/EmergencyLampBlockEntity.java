package com.triassicisland.light;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Lights for 20 minutes on its battery after a power cut; recharges (10 W) while the grid is up. */
public class EmergencyLampBlockEntity extends ElectricalBlockEntity {
    private static final double BATTERY_JOULES = 12_000;
    private static final double LAMP_WATTS = 10;
    private static final int LIGHT = 13;

    private double energy;
    private boolean charging;

    public EmergencyLampBlockEntity(BlockPos pos, BlockState state) {
        super(LightEntities.EMERGENCY_LAMP.get(), pos, state);
    }

    private double rated() {
        return ((EmergencyLampBlock) getBlockState().getBlock()).ratedVoltage;
    }

    private boolean gridUp() {
        return Math.abs(voltage) >= rated() * 0.6;
    }

    @Override
    protected double ratedVoltage() {
        return rated();
    }

    @Override
    public double groundConductance() {
        return (charging ? LAMP_WATTS : 1) / (rated() * rated());
    }

    void serverTick() {
        boolean up = gridUp();
        charging = up && energy < BATTERY_JOULES;
        int light = 0;
        if (up) {
            if (charging) energy = Math.min(BATTERY_JOULES, energy + Math.abs(power()) / 20);
        } else if (energy > 0) {
            energy = Math.max(0, energy - LAMP_WATTS / 20);
            light = LIGHT;
        }
        BlockState state = getBlockState();
        if (state.getValue(LampSocketBlock.LIGHT) != light) level.setBlock(worldPosition, state.setValue(LampSocketBlock.LIGHT, light), 3);
        if (level.getGameTime() % 100 == 0) setChanged();
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  battery %.0f%%", 100 * energy / BATTERY_JOULES));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("battery", args -> 100 * energy / BATTERY_JOULES);
        methods.put("is_lit", args -> getBlockState().getValue(LampSocketBlock.LIGHT) > 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Battery", energy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getDouble("Battery");
    }
}
