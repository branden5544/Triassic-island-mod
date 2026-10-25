package com.triassicisland;

import com.mojang.logging.LogUtils;
import com.triassicisland.registry.ModBlockEntities;
import com.triassicisland.registry.ModBlocks;
import com.triassicisland.registry.ModCreativeTabs;
import com.triassicisland.registry.ModItems;
import com.triassicisland.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import com.triassicisland.dino.DinoEntities;
import com.triassicisland.light.LightBlocks;
import com.triassicisland.light.LightEntities;
import com.triassicisland.packet.ModNetwork;
import com.triassicisland.smart.SmartBlocks;
import com.triassicisland.smart.SmartEntities;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TriassicIsland.MODID)
public class TriassicIsland {
    public static final String MODID = "triassicisland";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TriassicIsland() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LightBlocks.init();
        LightEntities.init();
        SmartBlocks.init();
        com.triassicisland.park.ParkEntities.init();
        SmartEntities.init();
        if (net.minecraftforge.fml.ModList.get().isLoaded("create")) {
            com.triassicisland.compat.create.CreateCompat.init(modEventBus);
        }
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        DinoEntities.ENTITIES.register(modEventBus);
        ModNetwork.register();
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, ModConfig.SPEC);
        ModCreativeTabs.TABS.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
