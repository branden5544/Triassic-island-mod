package com.triassicisland.machine;

import com.triassicisland.dna.Species;
import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Injects a DNA sample into a chicken egg, producing a dinosaur egg with the sample's quality. */
public class EmbryoFabricatorBlockEntity extends MachineBlockEntity {
    private static final int DNA = 0, EGG = 1, OUT = 2, OUT_SYRINGE = 3;

    public EmbryoFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.EMBRYO_FABRICATOR, pos, state);
    }

    @Override
    protected boolean canWork() {
        return !stack(DNA).isEmpty() && !stack(EGG).isEmpty() && stack(OUT).isEmpty()
                && canOutput(OUT_SYRINGE, new ItemStack(ModItems.EMPTY_SYRINGE.get()));
    }

    @Override
    protected void craft() {
        Species species = Species.of(stack(DNA)).orElseThrow();
        int quality = Species.quality(stack(DNA));
        consume(DNA);
        consume(EGG);
        output(OUT, species.apply(new ItemStack(ModItems.DINOSAUR_EGG.get()), quality));
        output(OUT_SYRINGE, new ItemStack(ModItems.EMPTY_SYRINGE.get()));
    }
}
