package com.triassicisland.smart;

import com.triassicisland.registry.ModBlocks;
import com.triassicisland.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

public final class SmartBlocks {
    public static final RegistryObject<Block> WIFI_ROUTER = ModBlocks.BLOCKS.register("wifi_router",
            () -> new WifiRouterBlock(metal().noOcclusion()));
    public static final RegistryObject<Block> SMART_SWITCH = ModBlocks.BLOCKS.register("smart_switch",
            () -> new SmartSwitchBlock(metal()));
    public static final RegistryObject<Block> SLIDING_DOOR = ModBlocks.BLOCKS.register("sliding_glass_door",
            () -> new SlidingDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3f).sound(SoundType.GLASS).noOcclusion()));

    static {
        ModItems.ITEMS.register("wifi_router", () -> new BlockItem(WIFI_ROUTER.get(), new Item.Properties()));
        ModItems.ITEMS.register("smart_switch", () -> new BlockItem(SMART_SWITCH.get(), new Item.Properties()));
        ModItems.ITEMS.register("sliding_glass_door", () -> new DoubleHighBlockItem(SLIDING_DOOR.get(), new Item.Properties()));
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5f).sound(SoundType.METAL);
    }

    public static void init() {}

    private SmartBlocks() {}
}
