package com.triassicisland.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalOpenPacket(BlockPos pos) {
    static void encode(TerminalOpenPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
    }

    static TerminalOpenPacket decode(FriendlyByteBuf buf) {
        return new TerminalOpenPacket(buf.readBlockPos());
    }

    static void handle(TerminalOpenPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.triassicisland.client.ClientPacketHooks.openTerminal(p.pos));
        ctx.get().setPacketHandled(true);
    }
}
