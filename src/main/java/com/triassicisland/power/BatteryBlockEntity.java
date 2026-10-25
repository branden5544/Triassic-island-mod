package com.triassicisland.power;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A battery: a voltage source whose voltage drops as it empties, with internal resistance.
 * When the grid is above its voltage, current flows in and it charges; otherwise it discharges.
 */
public class BatteryBlockEntity extends ElectricalBlockEntity {
    /** Small: 50 V, 200 kJ, up to ~5 A. Large: 200 V, 2 MJ, up to ~10 A. */
    public enum Size {
        SMALL(Voltage.LOW, 200_000, 2), LARGE(Voltage.MEDIUM, 2_000_000, 4);

        final double nominalVoltage, capacity, internalResistance;

        Size(double nominalVoltage, double capacity, double internalResistance) {
            this.nominalVoltage = nominalVoltage;
            this.capacity = capacity;
            this.internalResistance = internalResistance;
        }
    }

    private double energy;

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY.get(), pos, state);
    }

    private Size size() {
        return getBlockState().getBlock() instanceof BatteryBlock battery ? battery.size : Size.SMALL;
    }

    @Override
    protected double ratedVoltage() {
        return size().nominalVoltage;
    }

    /** Takes up to {@code joules} out of the battery; returns how much it gave. */
    public double drain(double joules) {
        double taken = Math.min(energy, Math.max(0, joules));
        energy -= taken;
        setChanged();
        return taken;
    }

    private double charge() {
        return energy / size().capacity;
    }

    private double openCircuitVoltage() {
        return size().nominalVoltage * (0.8 + 0.2 * charge());
    }

    /** An empty battery only lets current in (charging), never out. */
    private boolean active() {
        return energy > 0 || voltage > openCircuitVoltage();
    }

    @Override
    public double groundConductance() {
        return active() ? 1 / size().internalResistance : 0;
    }

    @Override
    public double sourceCurrent() {
        return active() ? openCircuitVoltage() / size().internalResistance : 0;
    }

    @Override
    protected void onSolved() {
        // Positive current = charging.
        energy = Math.max(0, Math.min(size().capacity, energy + voltage * current / 20));
        setChanged();
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  %.0f%% charged", charge() * 100));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Energy", energy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getDouble("Energy");
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("charge", args -> charge() * 100);
        methods.put("energy", args -> energy);
        methods.put("capacity", args -> size().capacity);
    }
}
