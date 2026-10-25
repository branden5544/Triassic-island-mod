package com.triassicisland.machine;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.Voltage;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared logic for the DNA lab: an inventory, progress, hopper access, and grid power.
 * Subclasses say when they can work ({@link #canWork}) and what happens when a cycle finishes ({@link #craft}).
 *
 * <p>While working, a machine is a resistor sized to draw its rated watts at its rated voltage. Lower voltage means less power
 * and slower progress (power goes with voltage squared); no power means no progress. Well above 50 V it overheats
 * and explodes.
 */
public abstract class MachineBlockEntity extends ElectricalBlockEntity implements MenuProvider {
    public static final int DATA_PROGRESS = 0, DATA_MAX_PROGRESS = 1, DATA_TANK = 2, DATA_DECIVOLTS = 3, DATA_WATTS = 4, DATA_COUNT = 5;
    /** Below this fraction of rated power a machine stalls instead of crawling along. */
    private static final double MIN_POWER = 0.05;

    protected final MachineKind kind;
    protected final ItemStackHandler items;
    protected double progress;
    protected boolean working;
    private final LazyOptional<IItemHandler> automation;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progressData();
                case DATA_MAX_PROGRESS -> maxProgressData();
                case DATA_TANK -> getTank();
                // Container data is synced as shorts, so keep these small.
                case DATA_DECIVOLTS -> (int) Math.min(Short.MAX_VALUE, Math.round(voltage * 10));
                case DATA_WATTS -> (int) Math.min(Short.MAX_VALUE, Math.round(Math.abs(power())));
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected MachineBlockEntity(MachineKind kind, BlockPos pos, BlockState state) {
        super(ModBlockEntities.type(kind), pos, state);
        this.kind = kind;
        this.items = new ItemStackHandler(kind.slots.size()) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return kind.accepts(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.automation = LazyOptional.of(() -> new AutomationHandler(items, kind));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity machine) {
        machine.tick();
    }

    protected void tick() {
        // Power comes from the grid solve at the end of last tick.
        working = canWork();
        if (working) {
            double powerFactor = Math.min(1, power() / kind.watts);
            if (powerFactor >= MIN_POWER) progress += powerFactor;
            if (progress >= kind.processTime) {
                progress = 0;
                craft();
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    @Override
    public double groundConductance() {
        return working ? kind.watts / ((double) kind.voltage * kind.voltage) : 0;
    }

    @Override
    protected double ratedVoltage() {
        return kind.voltage;
    }

    /** Enough voltage to run at all (a quarter of rated gives about 5% speed). */
    public boolean hasPower() {
        return voltage >= kind.voltage * 0.25;
    }

    protected int progressData() {
        return (int) progress;
    }

    protected int maxProgressData() {
        return kind.processTime;
    }

    /** Whether inputs are present and the outputs have room. */
    protected abstract boolean canWork();

    /** Consume inputs and produce outputs. Only called after {@link #canWork} returned true. */
    protected abstract void craft();

    /** Fluid shown in the GUI tank, in mB. Only the Fossil Cleaner has one. */
    protected int getTank() {
        return 0;
    }

    protected ItemStack stack(int slot) {
        return items.getStackInSlot(slot);
    }

    protected void consume(int slot) {
        items.extractItem(slot, 1, false);
    }

    protected boolean canOutput(int slot, ItemStack result) {
        ItemStack current = stack(slot);
        return current.isEmpty() || (ItemStack.isSameItemSameTags(current, result)
                && current.getCount() + result.getCount() <= current.getMaxStackSize());
    }

    /** Puts a result in an output slot. Bypasses {@link ItemStackHandler#isItemValid}, which rejects outputs. */
    protected void output(int slot, ItemStack result) {
        ItemStack current = stack(slot);
        if (current.isEmpty()) {
            items.setStackInSlot(slot, result.copy());
        } else {
            current.grow(result.getCount());
            setChanged();
        }
    }

    public void dropContents() {
        if (level == null) return;
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (int i = 0; i < items.getSlots(); i++) stacks.add(items.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, stacks);
    }

    // --- Menu ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.triassicisland." + kind.id());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory, kind, items, data, ContainerLevelAccess.create(level, worldPosition));
    }

    // --- Save / load ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putDouble("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        progress = tag.getDouble("Progress");
    }

    // --- Hoppers / pipes ---

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return automation.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automation.invalidate();
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_working", args -> working);
        methods.put("progress", args -> kind.processTime <= 0 ? 0.0 : progress / kind.processTime);
    }
}
