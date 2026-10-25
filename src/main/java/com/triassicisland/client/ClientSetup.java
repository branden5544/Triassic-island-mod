package com.triassicisland.client;

import com.triassicisland.TriassicIsland;
import com.triassicisland.client.dino.DinosaurRenderer;
import com.triassicisland.dino.DinoEntities;
import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinoSpecs;
import com.triassicisland.light.LightEntities;
import com.triassicisland.light.SocketType;
import com.triassicisland.power.HiddenCableBlockEntity;
import com.triassicisland.registry.ModBlockEntities;
import com.triassicisland.registry.ModBlocks;
import com.triassicisland.registry.ModMenus;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TriassicIsland.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.MACHINE.get(), MachineScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOLAR_PANEL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WIND_TURBINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_FENCE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HIGH_VOLTAGE_FENCE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_FENCE_GATE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SECURITY_CAMERA.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MONITOR.get(), MonitorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WIND_TURBINE.get(), WindTurbineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.POLE_INSULATOR.get(), WireRenderer::new);
        event.registerBlockEntityRenderer(LightEntities.LAMP_SOCKET.get(), SwingingLampRenderer::new);
        event.registerBlockEntityRenderer(com.triassicisland.smart.SmartEntities.SLIDING_DOOR.get(), SlidingDoorRenderer::new);
        DinoEntities.types().forEach((species, type) -> {
            DinoSpec spec = DinoSpecs.get(species);
            event.registerEntityRenderer(type.get(), context -> new DinosaurRenderer(context, spec));
        });
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        for (SocketType type : SocketType.values()) {
            if (!type.swings) continue;
            for (String look : new String[]{"empty", "off", "on"}) event.register(SwingingLampRenderer.hanging(type, look));
        }
    }

    /** Wraps every hidden cable model so it can render its disguise. */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var block : ModBlocks.HIDDEN_CABLES) {
            for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
                event.getModels().computeIfPresent(BlockModelShaper.stateToModelLocation(state), (key, model) -> new HiddenCableModel(model));
            }
        }
    }

    /** Disguised as grass or leaves? Use that block's tint. */
    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor color = (state, level, pos, tintIndex) -> {
            if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof HiddenCableBlockEntity cable) || cable.disguise() == null) {
                return -1;
            }
            return event.getBlockColors().getColor(cable.disguise(), level, pos, tintIndex);
        };
        ModBlocks.HIDDEN_CABLES.forEach(block -> event.register(color, block.get()));
    }

    private ClientSetup() {}
}
