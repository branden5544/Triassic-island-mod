package com.triassicisland.park;

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
import net.minecraftforge.registries.RegistryObject;

/** Fence decorations: pillars (conduct), warning lights, caps and footings. */
public final class ParkEntities {
    public static final RegistryObject<Block> STEEL_FENCE_PILLAR = ModBlocks.BLOCKS.register("steel_fence_pillar",
            () -> new FencePillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5f, 8f).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CONCRETE_FENCE_PILLAR = ModBlocks.BLOCKS.register("concrete_fence_pillar",
            () -> new FencePillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4f, 9f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> FENCE_WARNING_LIGHT = ModBlocks.BLOCKS.register("fence_warning_light",
            () -> new WarningLightBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1f).sound(SoundType.GLASS).noOcclusion()));
    public static final RegistryObject<Block> PILLAR_CAP = ModBlocks.BLOCKS.register("fence_pillar_cap",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2f).sound(SoundType.STONE).noOcclusion(),
                    Block.box(2, 0, 2, 14, 4, 14)));
    public static final RegistryObject<Block> FENCE_FOOTING = ModBlocks.BLOCKS.register("fence_footing",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3f, 9f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

    static {
        for (RegistryObject<Block> block : java.util.List.of(STEEL_FENCE_PILLAR, CONCRETE_FENCE_PILLAR, FENCE_WARNING_LIGHT, PILLAR_CAP, FENCE_FOOTING)) {
            ModItems.ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    public static final RegistryObject<BlockEntityType<FencePillarBlockEntity>> FENCE_PILLAR = ModBlockEntities.BLOCK_ENTITIES.register("fence_pillar",
            () -> BlockEntityType.Builder.of(FencePillarBlockEntity::new, STEEL_FENCE_PILLAR.get(), CONCRETE_FENCE_PILLAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<WarningLightBlockEntity>> WARNING_LIGHT = ModBlockEntities.BLOCK_ENTITIES.register("fence_warning_light",
            () -> BlockEntityType.Builder.of(WarningLightBlockEntity::new, FENCE_WARNING_LIGHT.get()).build(null));

    public static void init() {}

    private ParkEntities() {}
}
