package com.triassicisland.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Tells a client to look through security cameras: every camera on the network, starting at {@code start}. */
public record CameraViewPacket(BlockPos computer, List<Camera> cameras, int start) {
    public record Camera(String id, String label, BlockPos pos, float yaw, boolean powered) {}

    static void encode(CameraViewPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.computer);
        buf.writeVarInt(p.cameras.size());
        for (Camera c : p.cameras) {
            buf.writeUtf(c.id, 64);
            buf.writeUtf(c.label, 64);
            buf.writeBlockPos(c.pos);
            buf.writeFloat(c.yaw);
            buf.writeBoolean(c.powered);
        }
        buf.writeVarInt(p.start);
    }

    static CameraViewPacket decode(FriendlyByteBuf buf) {
        BlockPos computer = buf.readBlockPos();
        int count = Math.min(buf.readVarInt(), 256);
        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cameras.add(new Camera(buf.readUtf(64), buf.readUtf(64), buf.readBlockPos(), buf.readFloat(), buf.readBoolean()));
        }
        return new CameraViewPacket(computer, cameras, buf.readVarInt());
    }

    static void handle(CameraViewPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.triassicisland.client.ClientPacketHooks.viewCameras(p));
        ctx.get().setPacketHandled(true);
    }
}
