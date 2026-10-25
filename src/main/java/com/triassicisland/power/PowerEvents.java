package com.triassicisland.power;

import com.triassicisland.TriassicIsland;
import com.triassicisland.computer.ScriptRunner;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TriassicIsland.MODID)
public final class PowerEvents {
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        // Block entities tick first and decide what they draw; then the grid is solved for next tick.
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            GridManager.get(level).tick();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ScriptRunner.killAll();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        GridManager.clearAll();
        com.triassicisland.smart.Wifi.clear();
    }

    private PowerEvents() {}
}
