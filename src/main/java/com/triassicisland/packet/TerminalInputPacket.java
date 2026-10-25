package com.triassicisland.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalInputPacket(BlockPos pos, String line) {
    public static final int MAX_LENGTH = 256;

    static void encode(TerminalInputPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.line, MAX_LENGTH);
    }

    static TerminalInputPacket decode(FriendlyByteBuf buf) {
        return new TerminalInputPacket(buf.readBlockPos(), buf.readUtf(MAX_LENGTH));
    }

    static void handle(TerminalInputPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerComputers.with(ctx.get().getSender(), p.pos, computer -> computer.handleInput(p.line));
        ctx.get().setPacketHandled(true);
    }
}
