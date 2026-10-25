package com.triassicisland.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Electrical Age's macerator: grinds ores into double output, cobble into gravel into sand, and so on. */
public class MaceratorBlockEntity extends MachineBlockEntity {
    private static final int IN = 0, OUT = 1;

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.MACERATOR, pos, state);
    }

    @Override
    protected boolean canWork() {
        ItemStack result = MaceratorRecipes.result(stack(IN));
        return !result.isEmpty() && canOutput(OUT, result);
    }

    @Override
    protected void craft() {
        ItemStack result = MaceratorRecipes.result(stack(IN));
        consume(IN);
        output(OUT, result);
    }
}
