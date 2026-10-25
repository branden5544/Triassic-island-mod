package com.triassicisland.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/** What hoppers and pipes see: they can insert into input slots and only pull from output slots. */
class AutomationHandler implements IItemHandler {
    private final ItemStackHandler items;
    private final MachineKind kind;

    AutomationHandler(ItemStackHandler items, MachineKind kind) {
        this.items = items;
        this.kind = kind;
    }

    @Override
    public int getSlots() {
        return items.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return items.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return items.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return kind.slots.get(slot).output() ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return items.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return items.isItemValid(slot, stack);
    }
}
