package com.triassicisland.machine;

import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/** Washes fossil fragments with water. Usually yields a cleaned fossil, sometimes just bone meal. */
public class FossilCleanerBlockEntity extends MachineBlockEntity {
    public static final int TANK_CAPACITY = 4000;
    private static final int WATER_PER_CLEAN = 250;
    private static final float FOSSIL_CHANCE = 0.7f;
    private static final int IN = 0, BUCKET = 1, OUT_FOSSIL = 2, OUT_JUNK = 3;

    private int water;

    public FossilCleanerBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.FOSSIL_CLEANER, pos, state);
    }

    @Override
    protected void tick() {
        // Empty water buckets into the tank, leaving the empty bucket in the slot to take back.
        if (stack(BUCKET).is(Items.WATER_BUCKET) && water + 1000 <= TANK_CAPACITY) {
            water += 1000;
            items.setStackInSlot(BUCKET, new ItemStack(Items.BUCKET));
        }
        super.tick();
    }

    @Override
    protected boolean canWork() {
        return !stack(IN).isEmpty() && water >= WATER_PER_CLEAN
                && canOutput(OUT_FOSSIL, new ItemStack(ModItems.CLEANED_FOSSIL.get()))
                && canOutput(OUT_JUNK, new ItemStack(Items.BONE_MEAL));
    }

    @Override
    protected void craft() {
        consume(IN);
        water -= WATER_PER_CLEAN;
        if (level.random.nextFloat() < FOSSIL_CHANCE) {
            output(OUT_FOSSIL, new ItemStack(ModItems.CLEANED_FOSSIL.get()));
        } else {
            output(OUT_JUNK, new ItemStack(Items.BONE_MEAL));
        }
    }

    @Override
    protected int getTank() {
        return water;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Water", water);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        water = tag.getInt("Water");
    }
}
