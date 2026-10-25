package com.triassicisland.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Hands out permanent device ids like "circuit_breaker_3", counting separately for each device type. Saved with the world. */
public class DeviceIds extends SavedData {
    private final CompoundTag counters;

    private DeviceIds(CompoundTag counters) {
        this.counters = counters;
    }

    public static DeviceIds get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                tag -> new DeviceIds(tag.getCompound("Counters")), () -> new DeviceIds(new CompoundTag()), "triassicisland_devices");
    }

    public String next(String type) {
        int n = counters.getInt(type) + 1;
        counters.putInt(type, n);
        setDirty();
        return type + "_" + n;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("Counters", counters);
        return tag;
    }
}
