package com.triassicisland.power;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

public class BreakerBlockEntity extends ElectricalBlockEntity {
    private static final int[] RATINGS = {5, 10, 20, 40};
    private static final double OPEN_RESISTANCE = 1e9;

    private int ratingIndex = 2;
    private double amps;

    public BreakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREAKER.get(), pos, state);
    }

    private boolean open() {
        return getBlockState().getValue(BreakerBlock.OPEN);
    }

    public int cycleRating() {
        ratingIndex = (ratingIndex + 1) % RATINGS.length;
        setChanged();
        return RATINGS[ratingIndex];
    }

    @Override
    public double linkResistance() {
        return open() ? OPEN_RESISTANCE : 0.001;
    }

    @Override
    public void onLinkCurrent(double amps) {
        this.amps = amps;
        if (!open() && amps > RATINGS[ratingIndex] && level != null) {
            BreakerBlock.setOpen(level, worldPosition, getBlockState(), true);
        }
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  %s, %.1f / %d A", open() ? "OPEN" : "closed", amps, RATINGS[ratingIndex]));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Rating", ratingIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ratingIndex = Math.floorMod(tag.getInt("Rating"), RATINGS.length);
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_open", args -> open());
        methods.put("open", args -> {
            if (!open()) BreakerBlock.setOpen(level, worldPosition, getBlockState(), true);
            return null;
        });
        methods.put("close", args -> {
            if (open()) BreakerBlock.setOpen(level, worldPosition, getBlockState(), false);
            return null;
        });
        methods.put("amps", args -> amps);
        methods.put("rating", args -> RATINGS[ratingIndex]);
        methods.put("set_rating", args -> {
            int wanted = (int) DeviceMethod.number(args, 0);
            for (int i = 0; i < RATINGS.length; i++) {
                if (RATINGS[i] == wanted) {
                    ratingIndex = i;
                    setChanged();
                    return null;
                }
            }
            throw new IllegalArgumentException("rating must be 5, 10, 20 or 40");
        });
    }
}
