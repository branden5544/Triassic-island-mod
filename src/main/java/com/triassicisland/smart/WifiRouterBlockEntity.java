package com.triassicisland.smart;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.Voltage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** A 50 V, 10 W WiFi router with a 48 block range. */
public class WifiRouterBlockEntity extends ElectricalBlockEntity {
    public static final double RANGE = 48;
    private static final double WATTS = 10, MIN_VOLTS = 35;

    public WifiRouterBlockEntity(BlockPos pos, BlockState state) {
        super(SmartEntities.WIFI_ROUTER.get(), pos, state);
    }

    public boolean isPowered() {
        return Math.abs(voltage) >= MIN_VOLTS;
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.LOW;
    }

    @Override
    public double groundConductance() {
        return WATTS / (Voltage.LOW * Voltage.LOW);
    }

    void serverTick() {
        if (level == null || level.getGameTime() % 10 != 0) return;
        BlockState state = getBlockState();
        if (state.getValue(WifiRouterBlock.POWERED) != isPowered()) level.setBlock(worldPosition, state.setValue(WifiRouterBlock.POWERED, isPowered()), 3);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) Wifi.addRouter(this);
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel) Wifi.removeRouter(this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel) Wifi.removeRouter(this);
        super.onChunkUnloaded();
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(isPowered() ? String.format("  |  WiFi on, %.0f block range", RANGE) : "  |  WiFi off (no power)");
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_on", args -> isPowered());
        methods.put("range", args -> RANGE);
        methods.put("devices", args -> Wifi.wirelessNear(level, worldPosition).size());
    }
}
