package com.triassicisland.packet;

import com.triassicisland.computer.ComputerFiles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EditorSavePacket(BlockPos pos, String name, String text) {
    static void encode(EditorSavePacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.name, 64);
        buf.writeUtf(p.text, EditorOpenPacket.MAX_BYTES);
    }

    static EditorSavePacket decode(FriendlyByteBuf buf) {
        return new EditorSavePacket(buf.readBlockPos(), buf.readUtf(64), buf.readUtf(EditorOpenPacket.MAX_BYTES));
    }

    static void handle(EditorSavePacket p, Supplier<NetworkEvent.Context> ctx) {
        if (ComputerFiles.isValidName(p.name)) {
            ServerComputers.with(ctx.get().getSender(), p.pos, computer -> computer.saveFile(p.name, p.text));
        }
        ctx.get().setPacketHandled(true);
    }
}
