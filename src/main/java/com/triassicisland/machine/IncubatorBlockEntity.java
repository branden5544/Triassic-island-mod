package com.triassicisland.machine;

import com.triassicisland.dna.GeneticItem;
import com.triassicisland.dna.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Keeps a dinosaur egg warm until it's ready to hatch. The DNA quality is the chance the embryo survives;
 * failed eggs come out as bone meal. Hatching itself arrives with the dinosaurs.
 */
public class IncubatorBlockEntity extends MachineBlockEntity {
    private static final int IN = 0, OUT = 1;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.INCUBATOR, pos, state);
    }

    @Override
    protected boolean canWork() {
        return !stack(IN).isEmpty() && stack(OUT).isEmpty();
    }

    @Override
    protected void craft() {
        ItemStack egg = stack(IN).copyWithCount(1);
        boolean survived = level.random.nextInt(100) < Species.quality(egg);
        consume(IN);
        output(OUT, survived ? GeneticItem.markIncubated(egg) : new ItemStack(Items.BONE_MEAL, 2));
    }
}
