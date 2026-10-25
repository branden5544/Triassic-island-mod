package com.triassicisland.machine;

import com.triassicisland.power.Chargeable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electrical Age's battery charger: draws power while anything in it needs charge. At full power an item goes from
 * empty to full in 60 seconds (50 V charger) or 15 seconds (200 V); low voltage charges proportionally slower.
 */
public class ChargerBlockEntity extends MachineBlockEntity {
    private static final double MIN_POWER = 0.05;

    public ChargerBlockEntity(MachineKind kind, BlockPos pos, BlockState state) {
        super(kind, pos, state);
    }

    static ChargerBlockEntity small(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(MachineKind.CHARGER_50V, pos, state);
    }

    static ChargerBlockEntity large(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(MachineKind.CHARGER_200V, pos, state);
    }

    private int secondsToFull() {
        return kind == MachineKind.CHARGER_200V ? 15 : 60;
    }

    @Override
    protected void tick() {
        working = canWork();
        if (!working) return;
        double powerFactor = Math.min(1, power() / kind.watts);
        if (powerFactor < MIN_POWER) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = stack(i);
            if (!(stack.getItem() instanceof Chargeable item)) continue;
            int max = item.getMaxCharge(stack), now = item.getCharge(stack);
            if (now >= max) continue;
            int step = (int) Math.ceil(max * powerFactor / (secondsToFull() * 20.0));
            item.setCharge(stack, Math.min(max, now + step));
        }
        setChanged();
    }

    @Override
    protected boolean canWork() {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = stack(i);
            if (stack.getItem() instanceof Chargeable item && item.getCharge(stack) < item.getMaxCharge(stack)) return true;
        }
        return false;
    }

    @Override
    protected void craft() {}

    /** The GUI bar shows the average charge of what's inside, out of 1000. */
    @Override
    protected int progressData() {
        int count = 0;
        double total = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = stack(i);
            if (!(stack.getItem() instanceof Chargeable item)) continue;
            total += item.getCharge(stack) / (double) item.getMaxCharge(stack);
            count++;
        }
        return count == 0 ? 0 : (int) Math.round(1000 * total / count);
    }

    @Override
    protected int maxProgressData() {
        return 1000;
    }
}
