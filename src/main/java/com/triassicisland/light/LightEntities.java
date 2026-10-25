package com.triassicisland.light;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public final class LightEntities {
    public static final RegistryObject<BlockEntityType<LampSocketBlockEntity>> LAMP_SOCKET = ModBlockEntities.BLOCK_ENTITIES.register("lamp_socket",
            () -> BlockEntityType.Builder.<LampSocketBlockEntity>of((pos, state) -> new LampSocketBlockEntity(LightEntities.LAMP_SOCKET.get(), pos, state),
                    LightBlocks.SOCKETS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<StreetLightBlockEntity>> STREET_LIGHT = ModBlockEntities.BLOCK_ENTITIES.register("street_light",
            () -> BlockEntityType.Builder.of(StreetLightBlockEntity::new, LightBlocks.STREET_LIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<StringLightsBlockEntity>> STRING_LIGHTS = ModBlockEntities.BLOCK_ENTITIES.register("string_lights",
            () -> BlockEntityType.Builder.of(StringLightsBlockEntity::new, LightBlocks.STRING_LIGHTS.get()).build(null));
    public static final RegistryObject<BlockEntityType<LampSupplyBlockEntity>> LAMP_SUPPLY = ModBlockEntities.BLOCK_ENTITIES.register("lamp_supply",
            () -> BlockEntityType.Builder.of(LampSupplyBlockEntity::new, LightBlocks.LAMP_SUPPLY.get()).build(null));
    public static final RegistryObject<BlockEntityType<EmergencyLampBlockEntity>> EMERGENCY_LAMP = ModBlockEntities.BLOCK_ENTITIES.register("emergency_lamp",
            () -> BlockEntityType.Builder.of(EmergencyLampBlockEntity::new, LightBlocks.EMERGENCY_LAMP_50V.get(), LightBlocks.EMERGENCY_LAMP_200V.get()).build(null));

    public static void init() {}

    private LightEntities() {}
}
