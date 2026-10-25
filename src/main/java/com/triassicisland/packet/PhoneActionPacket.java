package com.triassicisland.packet;

import com.triassicisland.phone.PhoneService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** A button on the phone screen: refresh, close, send, call, answer, decline or hangup. */
public record PhoneActionPacket(String action, UUID target, String text) {
    static void encode(PhoneActionPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.action, 16);
        buf.writeUUID(p.target);
        buf.writeUtf(p.text, PhoneService.MAX_TEXT * 2);
    }

    static PhoneActionPacket decode(FriendlyByteBuf buf) {
        return new PhoneActionPacket(buf.readUtf(16), buf.readUUID(), buf.readUtf(PhoneService.MAX_TEXT * 2));
    }

    static void handle(PhoneActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        if (player != null) PhoneService.handle(player, p.action, p.target, p.text);
        ctx.get().setPacketHandled(true);
    }
}
