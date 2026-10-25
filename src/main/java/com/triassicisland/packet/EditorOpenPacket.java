package com.triassicisland.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EditorOpenPacket(BlockPos pos, String name, String text) {
    /** Client-to-server packets are capped at 32 KiB, so files edited in-game stay under this. */
    public static final int MAX_BYTES = 30000;

    static void encode(EditorOpenPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.name, 64);
        buf.writeUtf(p.text, 1 << 20);
    }

    static EditorOpenPacket decode(FriendlyByteBuf buf) {
        return new EditorOpenPacket(buf.readBlockPos(), buf.readUtf(64), buf.readUtf(1 << 20));
    }

    static void handle(EditorOpenPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.triassicisland.client.ClientPacketHooks.openEditor(p));
        ctx.get().setPacketHandled(true);
    }
}
