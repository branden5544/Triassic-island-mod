package com.triassicisland.smart;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public final class SmartEntities {
    public static final RegistryObject<BlockEntityType<WifiRouterBlockEntity>> WIFI_ROUTER = ModBlockEntities.BLOCK_ENTITIES.register("wifi_router",
            () -> BlockEntityType.Builder.of(WifiRouterBlockEntity::new, SmartBlocks.WIFI_ROUTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<SmartSwitchBlockEntity>> SMART_SWITCH = ModBlockEntities.BLOCK_ENTITIES.register("smart_switch",
            () -> BlockEntityType.Builder.of(SmartSwitchBlockEntity::new, SmartBlocks.SMART_SWITCH.get()).build(null));
    public static final RegistryObject<BlockEntityType<SlidingDoorBlockEntity>> SLIDING_DOOR = ModBlockEntities.BLOCK_ENTITIES.register("sliding_glass_door",
            () -> BlockEntityType.Builder.of(SlidingDoorBlockEntity::new, SmartBlocks.SLIDING_DOOR.get()).build(null));

    public static void init() {}

    private SmartEntities() {}
}
