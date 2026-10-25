package com.triassicisland.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** A terminal button: "terminate", "reboot", "power" or "close". */
public record TerminalActionPacket(BlockPos pos, String action) {
    static void encode(TerminalActionPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.action, 16);
    }

    static TerminalActionPacket decode(FriendlyByteBuf buf) {
        return new TerminalActionPacket(buf.readBlockPos(), buf.readUtf(16));
    }

    static void handle(TerminalActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        ServerComputers.with(player, p.pos, computer -> computer.handleAction(player, p.action));
        ctx.get().setPacketHandled(true);
    }
}
