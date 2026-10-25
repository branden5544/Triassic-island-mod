package com.triassicisland.smart;

import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks loaded WiFi routers and wireless devices per dimension (server side). Phones need a powered router in
 * range; computers reach wireless devices through any powered router on their data network.
 */
public final class Wifi {
    private static final Map<ResourceKey<Level>, Set<WifiRouterBlockEntity>> ROUTERS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<ElectricalBlockEntity>> WIRELESS = new HashMap<>();

    static void addRouter(WifiRouterBlockEntity router) {
        ROUTERS.computeIfAbsent(router.getLevel().dimension(), k -> new LinkedHashSet<>()).add(router);
    }

    static void removeRouter(WifiRouterBlockEntity router) {
        Set<WifiRouterBlockEntity> set = ROUTERS.get(router.getLevel().dimension());
        if (set != null) set.remove(router);
    }

    static void addWireless(ElectricalBlockEntity device) {
        WIRELESS.computeIfAbsent(device.getLevel().dimension(), k -> new LinkedHashSet<>()).add(device);
    }

    static void removeWireless(ElectricalBlockEntity device) {
        Set<ElectricalBlockEntity> set = WIRELESS.get(device.getLevel().dimension());
        if (set != null) set.remove(device);
    }

    public static void clear() {
        ROUTERS.clear();
        WIRELESS.clear();
    }

    /** Signal bars (0-4) for a player: 0 means no powered router in range. */
    public static int signal(ServerPlayer player) {
        double best = 0;
        for (WifiRouterBlockEntity router : ROUTERS.getOrDefault(player.level().dimension(), Set.of())) {
            if (!router.isPowered() || router.isRemoved()) continue;
            double d = Math.sqrt(player.distanceToSqr(router.getBlockPos().getCenter()));
            if (d <= WifiRouterBlockEntity.RANGE) best = Math.max(best, 1 - d / WifiRouterBlockEntity.RANGE);
        }
        return best <= 0 ? 0 : Math.max(1, (int) Math.ceil(best * 4));
    }

    /** Wireless devices within range of a router. */
    public static List<ElectricalBlockEntity> wirelessNear(Level level, BlockPos router) {
        List<ElectricalBlockEntity> found = new ArrayList<>();
        double range = WifiRouterBlockEntity.RANGE * WifiRouterBlockEntity.RANGE;
        for (ElectricalBlockEntity device : WIRELESS.getOrDefault(level.dimension(), Set.of())) {
            if (!device.isRemoved() && device.getBlockPos().distSqr(router) <= range) found.add(device);
        }
        return found;
    }

    private Wifi() {}
}
