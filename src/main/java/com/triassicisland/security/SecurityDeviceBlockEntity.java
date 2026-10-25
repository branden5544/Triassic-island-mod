package com.triassicisland.security;

import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.Voltage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A small 50 V device that works while it has at least 35 V. */
public abstract class SecurityDeviceBlockEntity extends ElectricalBlockEntity {
    private static final double MIN_VOLTS = 35;

    protected SecurityDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract double watts();

    public boolean isPowered() {
        return voltage >= MIN_VOLTS;
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.LOW;
    }

    @Override
    public double groundConductance() {
        return watts() / (Voltage.LOW * Voltage.LOW);
    }

    protected void serverTick() {}

    @Override
    protected void addDeviceMethods(java.util.Map<String, com.triassicisland.network.DeviceMethod> methods) {
        methods.put("is_powered", args -> isPowered());
    }
}
