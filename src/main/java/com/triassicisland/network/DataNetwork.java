package com.triassicisland.network;

import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.smart.Wifi;
import com.triassicisland.smart.WifiRouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds the devices reachable from a block through data cables (plus wireless devices near any powered WiFi router
 * on the network). Scanned on demand; networks are small and calls are rare.
 */
public final class DataNetwork {
    private static final int MAX_BLOCKS = 8192;

    /** Devices on the same network as {@code origin} (not including the origin itself). Server thread only. */
    public static List<ElectricalBlockEntity> devices(Level level, BlockPos origin) {
        List<ElectricalBlockEntity> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        seen.add(origin);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        while (!queue.isEmpty() && seen.size() < MAX_BLOCKS) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DataBlock block)) continue;
            for (Direction dir : Direction.values()) {
                if (!block.connectsData(state, dir)) continue;
                BlockPos next = pos.relative(dir);
                if (seen.contains(next) || !level.isLoaded(next)) continue;
                BlockState nextState = level.getBlockState(next);
                if (!(nextState.getBlock() instanceof DataBlock nextBlock) || !nextBlock.connectsData(nextState, dir.getOpposite())) continue;
                seen.add(next);
                if (nextBlock.isDataCable()) {
                    queue.add(next);
                } else if (level.getBlockEntity(next) instanceof ElectricalBlockEntity device) {
                    found.add(device);
                }
            }
        }
        // Powered WiFi routers on this network also reach the wireless devices around them.
        for (ElectricalBlockEntity device : List.copyOf(found)) {
            if (device instanceof WifiRouterBlockEntity router && router.isPowered()) {
                for (ElectricalBlockEntity wireless : Wifi.wirelessNear(level, router.getBlockPos())) {
                    if (!found.contains(wireless) && !wireless.getBlockPos().equals(origin)) found.add(wireless);
                }
            }
        }
        return found;
    }

    public static ElectricalBlockEntity find(Level level, BlockPos origin, String id) {
        for (ElectricalBlockEntity device : devices(level, origin)) {
            if (device.deviceId().equals(id)) return device;
        }
        return null;
    }

    private DataNetwork() {}
}
