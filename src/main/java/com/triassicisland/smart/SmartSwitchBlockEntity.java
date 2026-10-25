package com.triassicisland.smart;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class SmartSwitchBlockEntity extends ElectricalBlockEntity {
    private static final double OFF_RESISTANCE = 1e9;

    public SmartSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(SmartEntities.SMART_SWITCH.get(), pos, state);
    }

    private boolean on() {
        return getBlockState().getValue(SmartSwitchBlock.ON);
    }

    @Override
    public double linkResistance() {
        return on() ? 0.001 : OFF_RESISTANCE;
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(on() ? "  |  ON" : "  |  OFF");
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_on", args -> on());
        methods.put("turn_on", args -> {
            SmartSwitchBlock.set(level, worldPosition, getBlockState(), true);
            return null;
        });
        methods.put("turn_off", args -> {
            SmartSwitchBlock.set(level, worldPosition, getBlockState(), false);
            return null;
        });
        methods.put("toggle", args -> {
            SmartSwitchBlock.set(level, worldPosition, getBlockState(), !on());
            return on();
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) Wifi.addWireless(this);
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel) Wifi.removeWireless(this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel) Wifi.removeWireless(this);
        super.onChunkUnloaded();
    }
}
