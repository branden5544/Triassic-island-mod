package com.triassicisland.light;

import com.triassicisland.power.TowerPartBlock;
import com.triassicisland.power.Voltage;
import com.triassicisland.registry.ModBlocks;
import com.triassicisland.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

/** All the Electrical Age lights: sockets, bulbs, streetlight, string lights, lamp supply, emergency lamps, flashlights. */
public final class LightBlocks {
    public static final Map<SocketType, RegistryObject<Block>> SOCKETS = new EnumMap<>(SocketType.class);
    public static final Map<BulbType, RegistryObject<Item>> BULBS = new EnumMap<>(BulbType.class);

    static {
        for (SocketType type : SocketType.values()) {
            SOCKETS.put(type, ModBlocks.BLOCKS.register(type.id(), () -> new LampSocketBlock(type, light())));
        }
    }

    public static final RegistryObject<Block> STREET_LIGHT = ModBlocks.BLOCKS.register("street_light",
            () -> new StreetLightBlock(metal()));
    public static final RegistryObject<Block> STREET_LIGHT_POLE = ModBlocks.BLOCKS.register("street_light_pole",
            () -> new TowerPartBlock(metal().noLootTable(), () -> LightBlocks.STREET_LIGHT.get(), StreetLightBlock.HEIGHT,
                    Block.box(6, 0, 6, 10, 16, 10), false));
    public static final RegistryObject<Block> STREET_LIGHT_HEAD = ModBlocks.BLOCKS.register("street_light_head",
            () -> new StreetLightHeadBlock(metal().noLootTable()));
    public static final RegistryObject<Block> STRING_LIGHTS = ModBlocks.BLOCKS.register("string_lights",
            () -> new StringLightsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.3f)
                    .sound(SoundType.WOOL).noOcclusion()));
    public static final RegistryObject<Block> LAMP_SUPPLY = ModBlocks.BLOCKS.register("lamp_supply",
            () -> new LampSupplyBlock(metal()));
    public static final RegistryObject<Block> EMERGENCY_LAMP_50V = ModBlocks.BLOCKS.register("emergency_lamp_50v",
            () -> new EmergencyLampBlock(Voltage.LOW, light()));
    public static final RegistryObject<Block> EMERGENCY_LAMP_200V = ModBlocks.BLOCKS.register("emergency_lamp_200v",
            () -> new EmergencyLampBlock(Voltage.MEDIUM, light()));

    static {
        SOCKETS.values().forEach(LightBlocks::item);
        item(STREET_LIGHT);
        item(STRING_LIGHTS);
        item(LAMP_SUPPLY);
        item(EMERGENCY_LAMP_50V);
        item(EMERGENCY_LAMP_200V);
        for (BulbType type : BulbType.values()) {
            BULBS.put(type, ModItems.ITEMS.register(type.id(), () -> new BulbItem(type, new Item.Properties().stacksTo(16))));
        }
    }

    public static final RegistryObject<Item> SMALL_FLASHLIGHT = ModItems.ITEMS.register("small_flashlight",
            () -> new FlashlightItem(16, 12, 10, new Item.Properties()));
    public static final RegistryObject<Item> IMPROVED_FLASHLIGHT = ModItems.ITEMS.register("improved_flashlight",
            () -> new FlashlightItem(32, 15, 30, new Item.Properties()));

    private static void item(RegistryObject<Block> block) {
        ModItems.ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static BlockBehaviour.Properties light() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1f).sound(SoundType.LANTERN).noOcclusion();
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5f).sound(SoundType.METAL).noOcclusion();
    }

    /** Loads this class so its blocks and items are registered. */
    public static void init() {}

    private LightBlocks() {}
}
