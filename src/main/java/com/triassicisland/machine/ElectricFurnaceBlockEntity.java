package com.triassicisland.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/** Electrical Age's electrical furnace: smelts anything a furnace can, twice as fast, using 500 W instead of fuel. */
public class ElectricFurnaceBlockEntity extends MachineBlockEntity {
    private static final int IN = 0, OUT = 1;

    private ItemStack cachedInput = ItemStack.EMPTY;
    private ItemStack cachedResult = ItemStack.EMPTY;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(MachineKind.ELECTRIC_FURNACE, pos, state);
    }

    private ItemStack result() {
        ItemStack input = stack(IN);
        if (input.isEmpty()) return ItemStack.EMPTY;
        if (!ItemStack.isSameItemSameTags(input, cachedInput)) {
            cachedInput = input.copyWithCount(1);
            cachedResult = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(cachedInput), level)
                    .map(recipe -> recipe.getResultItem(level.registryAccess()).copy())
                    .orElse(ItemStack.EMPTY);
        }
        return cachedResult;
    }

    @Override
    protected boolean canWork() {
        ItemStack result = result();
        return !result.isEmpty() && canOutput(OUT, result);
    }

    @Override
    protected void craft() {
        ItemStack result = result().copy();
        consume(IN);
        output(OUT, result);
    }
}
