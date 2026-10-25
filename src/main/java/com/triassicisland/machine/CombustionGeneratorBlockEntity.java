package com.triassicisland.machine;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.Voltage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

/**
 * Burns furnace fuel. The 50 V combustion generator gives up to ~1.2 kW, the 200 V fuel generator ~2.5 kW.
 * Modelled as a voltage source with a small internal resistance, so heavy loads pull the voltage down and a
 * short circuit pushes enough current to burn cables. Fuel is only used for the power actually delivered.
 */
public class CombustionGeneratorBlockEntity extends MachineBlockEntity {
    /** Joules of electricity per tick of furnace burn time (one coal = 200 kJ). */
    private static final double JOULES_PER_BURN_TICK = 125;
    private static final int FUEL = 0;

    private double energy;
    private double lastFuelEnergy = 1;

    public CombustionGeneratorBlockEntity(MachineKind kind, BlockPos pos, BlockState state) {
        super(kind, pos, state);
    }

    static CombustionGeneratorBlockEntity combustion(BlockPos pos, BlockState state) {
        return new CombustionGeneratorBlockEntity(MachineKind.COMBUSTION_GENERATOR, pos, state);
    }

    static CombustionGeneratorBlockEntity fuel(BlockPos pos, BlockState state) {
        return new CombustionGeneratorBlockEntity(MachineKind.FUEL_GENERATOR, pos, state);
    }

    private double internalResistance() {
        // 0.5 ohm at 50 V; the 200 V generator is built twice as strong.
        double v = kind.voltage / Voltage.LOW;
        return kind == MachineKind.FUEL_GENERATOR ? 0.25 * v * v : 0.5;
    }

    @Override
    protected void tick() {
        if (energy <= 0 && !stack(FUEL).isEmpty()) {
            lastFuelEnergy = ForgeHooks.getBurnTime(stack(FUEL), null) * JOULES_PER_BURN_TICK;
            energy += lastFuelEnergy;
            // Lava buckets and the like leave their container behind.
            var remainder = stack(FUEL).getCraftingRemainingItem();
            consume(FUEL);
            if (!remainder.isEmpty() && stack(FUEL).isEmpty()) items.setStackInSlot(FUEL, remainder);
            setChanged();
        }
    }

    private boolean running() {
        return energy > 0;
    }

    @Override
    public double groundConductance() {
        return running() ? 1 / internalResistance() : 0;
    }

    @Override
    public double sourceCurrent() {
        return running() ? kind.voltage / internalResistance() : 0;
    }

    @Override
    protected void onSolved() {
        // current is negative while supplying.
        energy = Math.max(0, energy + voltage * current / 20);
    }

    @Override
    protected boolean canWork() {
        return false;
    }

    @Override
    protected void craft() {}

    /** The GUI bar shows fuel left in the current item, out of 1000. */
    @Override
    protected int progressData() {
        return (int) (1000 * Math.min(1, energy / lastFuelEnergy));
    }

    @Override
    protected int maxProgressData() {
        return 1000;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Energy", energy);
        tag.putDouble("LastFuelEnergy", lastFuelEnergy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = tag.getDouble("Energy");
        lastFuelEnergy = Math.max(1, tag.getDouble("LastFuelEnergy"));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("fuel", args -> stack(FUEL).getCount());
        methods.put("burning", args -> running());
    }
}
