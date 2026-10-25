package com.triassicisland.compat.create;

import com.simibubi.create.api.stress.BlockStressValues;
import com.triassicisland.power.Voltage;
import com.triassicisland.registry.ModBlockEntities;
import com.triassicisland.registry.ModBlocks;
import com.triassicisland.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Create integration: Kinetic Generators that turn rotation (stress units) into grid power, 1 SU = 1 W.
 * Only called when Create is installed, so none of these classes load otherwise.
 */
public final class CreateCompat {
    /** Stress per RPM each generator puts on the Create network. */
    public static final double IMPACT = 16;

    public static RegistryObject<Block> GENERATOR_50V, GENERATOR_200V;
    public static RegistryObject<BlockEntityType<KineticGeneratorBlockEntity>> GENERATOR;

    public static void init(IEventBus modBus) {
        GENERATOR_50V = ModBlocks.BLOCKS.register("kinetic_generator_50v", () -> new KineticGeneratorBlock(Voltage.LOW, properties()));
        GENERATOR_200V = ModBlocks.BLOCKS.register("kinetic_generator_200v", () -> new KineticGeneratorBlock(Voltage.MEDIUM, properties()));
        ModItems.ITEMS.register("kinetic_generator_50v", () -> new BlockItem(GENERATOR_50V.get(), new Item.Properties()));
        ModItems.ITEMS.register("kinetic_generator_200v", () -> new BlockItem(GENERATOR_200V.get(), new Item.Properties()));
        GENERATOR = ModBlockEntities.BLOCK_ENTITIES.register("kinetic_generator", () -> BlockEntityType.Builder.<KineticGeneratorBlockEntity>of(
                (pos, state) -> new KineticGeneratorBlockEntity(GENERATOR.get(), pos, state), GENERATOR_50V.get(), GENERATOR_200V.get()).build(null));
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            BlockStressValues.IMPACTS.register(GENERATOR_50V.get(), () -> IMPACT);
            BlockStressValues.IMPACTS.register(GENERATOR_200V.get(), () -> IMPACT);
        }));
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private CreateCompat() {}
}
