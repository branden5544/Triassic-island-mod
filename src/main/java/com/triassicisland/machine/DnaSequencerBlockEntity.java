package com.triassicisland.machine;

import com.triassicisland.dna.Species;
import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Sequences two samples of the same species to fill in each other's gaps.
 * The result keeps the better sample's quality plus half of the weaker one's, up to 100%.
 */
public class DnaSequencerBlockEntity extends MachineBlockEntity {
    private static final int A = 0, B = 1, OUT = 2, OUT_SYRINGE = 3;

    public DnaSequencerBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.DNA_SEQUENCER, pos, state);
    }

    @Override
    protected boolean canWork() {
        Optional<Species> a = Species.of(stack(A));
        return a.isPresent() && a.equals(Species.of(stack(B))) && stack(OUT).isEmpty()
                && canOutput(OUT_SYRINGE, new ItemStack(ModItems.EMPTY_SYRINGE.get()));
    }

    @Override
    protected void craft() {
        Species species = Species.of(stack(A)).orElseThrow();
        int qa = Species.quality(stack(A)), qb = Species.quality(stack(B));
        int combined = Math.max(qa, qb) + Math.min(qa, qb) / 2;
        consume(A);
        consume(B);
        output(OUT, species.apply(new ItemStack(ModItems.DNA_SAMPLE.get()), combined));
        output(OUT_SYRINGE, new ItemStack(ModItems.EMPTY_SYRINGE.get()));
    }
}
