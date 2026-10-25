package com.triassicisland.client;

import com.triassicisland.TriassicIsland;
import com.triassicisland.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Rebuilds the world's meshes when the player puts Engineer's Goggles on or takes them off, so hidden cables show or hide. */
@Mod.EventBusSubscriber(modid = TriassicIsland.MODID, value = Dist.CLIENT)
public final class GogglesWatcher {
    private static boolean revealed;

    public static boolean revealed() {
        return revealed;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        boolean wearing = mc.player != null && mc.player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ENGINEERS_GOGGLES.get());
        if (wearing != revealed) {
            revealed = wearing;
            if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
        }
    }

    private GogglesWatcher() {}
}
