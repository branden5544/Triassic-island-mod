package com.triassicisland.machine;

import com.triassicisland.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** One menu type serves every DNA lab machine; the {@link MachineKind} decides the slots. */
public class MachineMenu extends AbstractContainerMenu {
    private final MachineKind kind;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final int machineSlots;

    /** Client side: the server tells us which machine this is. */
    public static MachineMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buf) {
        MachineKind kind = MachineKind.values()[buf.readVarInt()];
        buf.readBlockPos();
        ItemStackHandler items = new ItemStackHandler(kind.slots.size()) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return kind.accepts(slot, stack);
            }
        };
        return new MachineMenu(id, inventory, kind, items, new SimpleContainerData(MachineBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public MachineMenu(int id, Inventory inventory, MachineKind kind, IItemHandler items, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.MACHINE.get(), id);
        this.kind = kind;
        this.data = data;
        this.access = access;
        this.machineSlots = kind.slots.size();

        for (int i = 0; i < machineSlots; i++) {
            MachineKind.SlotDef def = kind.slots.get(i);
            addSlot(def.output() ? new OutputSlot(items, i, def.x(), def.y()) : new SlotItemHandler(items, i, def.x(), def.y()));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public MachineKind kind() {
        return kind;
    }

    /** Progress from 0 to 1. */
    public float progress() {
        int max = data.get(MachineBlockEntity.DATA_MAX_PROGRESS);
        return max <= 0 ? 0 : (float) data.get(MachineBlockEntity.DATA_PROGRESS) / max;
    }

    public int decivolts() {
        return data.get(MachineBlockEntity.DATA_DECIVOLTS);
    }

    public int watts() {
        return data.get(MachineBlockEntity.DATA_WATTS);
    }

    public int tank() {
        return data.get(MachineBlockEntity.DATA_TANK);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < machineSlots) {
            // Machine -> player inventory
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // Player inventory -> first machine slot that accepts it
            boolean moved = false;
            for (int i = 0; i < machineSlots && !stack.isEmpty(); i++) {
                if (kind.accepts(i, stack)) moved |= moveItemStackTo(stack, i, i + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof MachineBlockEntity
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64, true);
    }

    private static class OutputSlot extends SlotItemHandler {
        OutputSlot(IItemHandler items, int index, int x, int y) {
            super(items, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}
