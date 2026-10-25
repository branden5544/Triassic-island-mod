package com.triassicisland.machine;

import com.triassicisland.dna.Species;
import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Draws DNA from a cleaned fossil or a mosquito in amber into a syringe. Amber DNA is better preserved. */
public class DnaExtractorBlockEntity extends MachineBlockEntity {
    private static final int SOURCE = 0, SYRINGE = 1, OUT = 2;

    public DnaExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.DNA_EXTRACTOR, pos, state);
    }

    @Override
    protected boolean canWork() {
        return !stack(SOURCE).isEmpty() && !stack(SYRINGE).isEmpty() && stack(OUT).isEmpty();
    }

    @Override
    protected void craft() {
        ItemStack source = stack(SOURCE);
        boolean amber = source.is(ModItems.MOSQUITO_IN_AMBER.get());
        // The species is only revealed once the DNA is read.
        Species species = Species.random(level.random);
        int quality = amber ? 30 + level.random.nextInt(41) : 10 + level.random.nextInt(31);
        consume(SOURCE);
        consume(SYRINGE);
        output(OUT, species.apply(new ItemStack(ModItems.DNA_SAMPLE.get()), quality));
    }
}
