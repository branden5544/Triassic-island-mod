package com.triassicisland.registry;

import com.triassicisland.TriassicIsland;
import com.triassicisland.dino.DinosaurEggItem;
import com.triassicisland.dna.GeneticItem;
import com.triassicisland.phone.PhoneItem;
import com.triassicisland.power.CableTier;
import com.triassicisland.power.FuseItem;
import com.triassicisland.power.WireCoilItem;
import com.triassicisland.power.GogglesItem;
import com.triassicisland.power.MultimeterItem;
import com.triassicisland.security.KeycardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TriassicIsland.MODID);

    public static final RegistryObject<Item> FOSSIL_FRAGMENT = simple("fossil_fragment");
    public static final RegistryObject<Item> AMBER = simple("amber");
    public static final RegistryObject<Item> MOSQUITO_IN_AMBER = simple("mosquito_in_amber");
    public static final RegistryObject<Item> EMPTY_SYRINGE = simple("empty_syringe");

    // DNA chain. Fossils stack; samples and eggs carry a species and quality.
    public static final RegistryObject<Item> CLEANED_FOSSIL = simple("cleaned_fossil");
    public static final RegistryObject<Item> DNA_SAMPLE = ITEMS.register("dna_sample",
            () -> new GeneticItem(new Item.Properties().stacksTo(1), true));
    public static final RegistryObject<Item> DINOSAUR_EGG = ITEMS.register("dinosaur_egg",
            () -> new DinosaurEggItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> MULTIMETER = ITEMS.register("multimeter",
            () -> new MultimeterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FOSSIL_ORE = block(ModBlocks.FOSSIL_ORE);
    public static final RegistryObject<Item> DEEPSLATE_FOSSIL_ORE = block(ModBlocks.DEEPSLATE_FOSSIL_ORE);
    public static final RegistryObject<Item> AMBER_ORE = block(ModBlocks.AMBER_ORE);

    static {
        ModBlocks.MACHINES.values().forEach(ModItems::block);
    }

    public static final RegistryObject<Item> LOW_VOLTAGE_CABLE = block(ModBlocks.LOW_VOLTAGE_CABLE);
    public static final RegistryObject<Item> MEDIUM_VOLTAGE_CABLE = block(ModBlocks.MEDIUM_VOLTAGE_CABLE);
    public static final RegistryObject<Item> HIGH_VOLTAGE_CABLE = block(ModBlocks.HIGH_VOLTAGE_CABLE);
    public static final RegistryObject<Item> TRANSFORMER = block(ModBlocks.TRANSFORMER);
    public static final RegistryObject<Item> CIRCUIT_BREAKER = block(ModBlocks.CIRCUIT_BREAKER);
    public static final RegistryObject<Item> BATTERY = block(ModBlocks.BATTERY);
    public static final RegistryObject<Item> LARGE_BATTERY = block(ModBlocks.LARGE_BATTERY);
    public static final RegistryObject<Item> SOLAR_PANEL = block(ModBlocks.SOLAR_PANEL);
    public static final RegistryObject<Item> WIND_TURBINE = block(ModBlocks.WIND_TURBINE);

    public static final RegistryObject<Item> ELECTRIC_FENCE = block(ModBlocks.ELECTRIC_FENCE);
    public static final RegistryObject<Item> HIGH_VOLTAGE_FENCE = block(ModBlocks.HIGH_VOLTAGE_FENCE);
    public static final RegistryObject<Item> ELECTRIC_FENCE_GATE = block(ModBlocks.ELECTRIC_FENCE_GATE);
    public static final RegistryObject<Item> FEEDER = block(ModBlocks.FEEDER);
    public static final RegistryObject<Item> HIGH_VOLTAGE_SIGN = block(ModBlocks.HIGH_VOLTAGE_SIGN);

    public static final RegistryObject<Item> DATA_CABLE = block(ModBlocks.DATA_CABLE);
    public static final RegistryObject<Item> COMBINED_CABLE = block(ModBlocks.COMBINED_CABLE);
    public static final RegistryObject<Item> HIDDEN_LOW_VOLTAGE_CABLE = block(ModBlocks.HIDDEN_LOW_VOLTAGE_CABLE);
    public static final RegistryObject<Item> HIDDEN_MEDIUM_VOLTAGE_CABLE = block(ModBlocks.HIDDEN_MEDIUM_VOLTAGE_CABLE);
    public static final RegistryObject<Item> HIDDEN_HIGH_VOLTAGE_CABLE = block(ModBlocks.HIDDEN_HIGH_VOLTAGE_CABLE);
    public static final RegistryObject<Item> HIDDEN_DATA_CABLE = block(ModBlocks.HIDDEN_DATA_CABLE);
    public static final RegistryObject<Item> HIDDEN_COMBINED_CABLE = block(ModBlocks.HIDDEN_COMBINED_CABLE);
    public static final RegistryObject<Item> COMPUTER = block(ModBlocks.COMPUTER);
    public static final RegistryObject<Item> ENGINEERS_GOGGLES = ITEMS.register("engineers_goggles",
            () -> new GogglesItem(new Item.Properties()));

    public static final RegistryObject<Item> SECURITY_CAMERA = block(ModBlocks.SECURITY_CAMERA);
    public static final RegistryObject<Item> MONITOR = block(ModBlocks.MONITOR);
    public static final RegistryObject<Item> KEYCARD_READER = block(ModBlocks.KEYCARD_READER);
    public static final RegistryObject<Item> ALARM = block(ModBlocks.ALARM);
    public static final List<RegistryObject<Item>> KEYCARDS = IntStream.rangeClosed(1, 5)
            .mapToObj(level -> ITEMS.register("keycard_" + level, () -> (Item) new KeycardItem(level, new Item.Properties().stacksTo(1))))
            .toList();

    public static final RegistryObject<Item> LAMP = block(ModBlocks.LAMP);
    public static final RegistryObject<Item> FLOODLIGHT = block(ModBlocks.FLOODLIGHT);
    public static final RegistryObject<Item> VARIABLE_TRANSFORMER = block(ModBlocks.VARIABLE_TRANSFORMER);
    public static final RegistryObject<Item> ENERGY_METER = block(ModBlocks.ENERGY_METER);
    public static final RegistryObject<Item> FUSE_HOLDER = block(ModBlocks.FUSE_HOLDER);
    public static final RegistryObject<Item> UTILITY_POLE = block(ModBlocks.UTILITY_POLE);
    public static final RegistryObject<Item> POLE_INSULATOR = block(ModBlocks.POLE_INSULATOR);
    public static final Map<CableTier, RegistryObject<Item>> WIRE_COILS = new EnumMap<>(CableTier.class);

    static {
        for (CableTier tier : CableTier.values()) {
            String name = tier.name().toLowerCase(java.util.Locale.ROOT) + "_voltage_wire_coil";
            WIRE_COILS.put(tier, ITEMS.register(name, () -> new WireCoilItem(tier, new Item.Properties())));
        }
    }

    public static Item wireCoil(CableTier tier) {
        return WIRE_COILS.get(tier).get();
    }

    public static final RegistryObject<Item> SMARTPHONE = ITEMS.register("smartphone", () -> new PhoneItem(new Item.Properties()));

    public static final int[] FUSE_RATINGS = {5, 10, 20, 40};
    public static final List<RegistryObject<Item>> FUSES = IntStream.of(FUSE_RATINGS)
            .mapToObj(amps -> ITEMS.register("fuse_" + amps + "a", () -> (Item) new FuseItem(amps, new Item.Properties())))
            .toList();

    public static ItemStack fuse(int amps) {
        for (int i = 0; i < FUSE_RATINGS.length; i++) {
            if (FUSE_RATINGS[i] == amps) return new ItemStack(FUSES.get(i).get());
        }
        return ItemStack.EMPTY;
    }

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> block(RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
