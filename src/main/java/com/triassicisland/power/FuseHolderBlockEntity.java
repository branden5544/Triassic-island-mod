package com.triassicisland.power;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** A fuse is a one-shot breaker: over its rating for a few ticks, it blows and has to be replaced. */
public class FuseHolderBlockEntity extends ElectricalBlockEntity {
    private static final double OPEN_RESISTANCE = 1e9;
    private static final int BLOW_TICKS = 3;

    /** Amps of the fitted fuse, or 0 when there is none (or it blew). */
    private int rating;
    private int overTicks;
    private double amps;

    public FuseHolderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSE_HOLDER.get(), pos, state);
    }

    int rating() {
        return rating;
    }

    void insert(int amps) {
        rating = amps;
        overTicks = 0;
        updateState();
    }

    ItemStack remove() {
        if (rating == 0) return ItemStack.EMPTY;
        ItemStack fuse = ModItems.fuse(rating);
        rating = 0;
        updateState();
        return fuse;
    }

    private void updateState() {
        setChanged();
        if (level != null) level.setBlock(worldPosition, getBlockState().setValue(FuseHolderBlock.HAS_FUSE, rating > 0), 3);
    }

    @Override
    public double linkResistance() {
        return rating > 0 ? 0.001 : OPEN_RESISTANCE;
    }

    @Override
    public void onLinkCurrent(double amps) {
        this.amps = amps;
        if (rating == 0) return;
        overTicks = amps > rating ? overTicks + 1 : 0;
        if (overTicks >= BLOW_TICKS && level instanceof ServerLevel server) {
            rating = 0;
            updateState();
            server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1.4f);
            server.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.02);
        }
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(rating > 0 ? String.format("  |  %d A fuse, %.1f A", rating, amps) : "  |  no fuse / blown");
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("rating", args -> rating);
        methods.put("has_fuse", args -> rating > 0);
        methods.put("amps", args -> amps);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Fuse", rating);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rating = tag.getInt("Fuse");
    }
}
