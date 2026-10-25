package com.triassicisland.machine;

import com.triassicisland.dna.GeneticItem;
import com.triassicisland.dna.Species;
import com.triassicisland.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Machines with a GUI: the DNA lab, Electrical Age-style processing machines, and the fuel generators. Slot layout lives here so the server menu, the client menu
 * and the screen all agree. Slot x/y are the item position inside the GUI.
 */
public enum MachineKind {
    FOSSIL_CLEANER(100, 200, FossilCleanerBlockEntity::new,
            in(56, 35, s -> s.is(ModItems.FOSSIL_FRAGMENT.get())),
            in(20, 53, s -> s.is(Items.WATER_BUCKET)),
            out(116, 35), out(134, 35)),
    DNA_EXTRACTOR(200, 300, DnaExtractorBlockEntity::new,
            in(56, 26, s -> s.is(ModItems.CLEANED_FOSSIL.get()) || s.is(ModItems.MOSQUITO_IN_AMBER.get())),
            in(56, 46, s -> s.is(ModItems.EMPTY_SYRINGE.get())),
            out(116, 35)),
    DNA_SEQUENCER(300, 400, DnaSequencerBlockEntity::new,
            in(56, 26, MachineKind::isDnaSample),
            in(56, 46, MachineKind::isDnaSample),
            out(116, 35), out(142, 35)),
    EMBRYO_FABRICATOR(250, 300, EmbryoFabricatorBlockEntity::new,
            in(56, 26, MachineKind::isDnaSample),
            in(56, 46, s -> s.is(Items.EGG)),
            out(116, 35), out(142, 35)),
    INCUBATOR(80, 1200, IncubatorBlockEntity::new,
            in(56, 35, s -> s.is(ModItems.DINOSAUR_EGG.get()) && Species.of(s).isPresent() && !GeneticItem.isIncubated(s)),
            out(116, 35)),
    ELECTRIC_FURNACE(500, 100, ElectricFurnaceBlockEntity::new,
            in(56, 35, s -> true),
            out(116, 35)),
    MACERATOR(300, 200, MaceratorBlockEntity::new,
            in(56, 35, MaceratorRecipes::canProcess),
            out(116, 35)),
    // Battery chargers fill flashlights and phones.
    CHARGER_50V(50, 100, 0, ChargerBlockEntity::small,
            charge(53), charge(71), charge(89), charge(107)),
    CHARGER_200V(200, 400, 0, ChargerBlockEntity::large,
            charge(53), charge(71), charge(89), charge(107)),
    // Sources, not loads: they burn furnace fuel.
    COMBUSTION_GENERATOR(0, 0, CombustionGeneratorBlockEntity::combustion,
            in(80, 53, s -> ForgeHooks.getBurnTime(s, null) > 0)),
    FUEL_GENERATOR(200, 0, 0, CombustionGeneratorBlockEntity::fuel,
            in(80, 53, s -> ForgeHooks.getBurnTime(s, null) > 0));

    public record SlotDef(int x, int y, boolean output, Predicate<ItemStack> accepts) {}

    /** Rated voltage. */
    public final int voltage;
    /** Power drawn at the rated voltage while working, in watts. */
    public final int watts;
    /** Ticks per cycle at full power. */
    public final int processTime;
    public final BlockEntityType.BlockEntitySupplier<MachineBlockEntity> factory;
    public final List<SlotDef> slots;

    MachineKind(int watts, int processTime, BlockEntityType.BlockEntitySupplier<MachineBlockEntity> factory, SlotDef... slots) {
        this(50, watts, processTime, factory, slots);
    }

    MachineKind(int voltage, int watts, int processTime, BlockEntityType.BlockEntitySupplier<MachineBlockEntity> factory, SlotDef... slots) {
        this.voltage = voltage;
        this.watts = watts;
        this.processTime = processTime;
        this.factory = factory;
        this.slots = List.of(slots);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isGenerator() {
        return this == COMBUSTION_GENERATOR || this == FUEL_GENERATOR;
    }

    public boolean isCharger() {
        return this == CHARGER_50V || this == CHARGER_200V;
    }

    public boolean hasWaterTank() {
        return this == FOSSIL_CLEANER;
    }

    public boolean accepts(int slot, ItemStack stack) {
        SlotDef def = slots.get(slot);
        return !def.output() && def.accepts().test(stack);
    }

    private static boolean isDnaSample(ItemStack stack) {
        return stack.is(ModItems.DNA_SAMPLE.get()) && Species.of(stack).isPresent();
    }

    private static SlotDef in(int x, int y, Predicate<ItemStack> accepts) {
        return new SlotDef(x, y, false, accepts);
    }

    private static SlotDef charge(int x) {
        return new SlotDef(x, 30, false, s -> s.getItem() instanceof com.triassicisland.power.Chargeable);
    }

    private static SlotDef out(int x, int y) {
        return new SlotDef(x, y, true, s -> false);
    }
}
