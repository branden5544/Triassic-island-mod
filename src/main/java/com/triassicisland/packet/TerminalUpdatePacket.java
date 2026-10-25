package com.triassicisland.packet;

import com.triassicisland.computer.Terminal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalUpdatePacket(BlockPos pos, String[] lines, int cursorX, int cursorY, boolean running) {
    static void encode(TerminalUpdatePacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        for (String line : p.lines) buf.writeUtf(line, Terminal.WIDTH * 3);
        buf.writeVarInt(p.cursorX);
        buf.writeVarInt(p.cursorY);
        buf.writeBoolean(p.running);
    }

    static TerminalUpdatePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String[] lines = new String[Terminal.HEIGHT];
        for (int i = 0; i < lines.length; i++) lines[i] = buf.readUtf(Terminal.WIDTH * 3);
        return new TerminalUpdatePacket(pos, lines, buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    static void handle(TerminalUpdatePacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.triassicisland.client.ClientPacketHooks.updateTerminal(p));
        ctx.get().setPacketHandled(true);
    }
}
