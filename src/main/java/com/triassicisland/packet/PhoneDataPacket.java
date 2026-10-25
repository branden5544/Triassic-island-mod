package com.triassicisland.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Everything the phone screen shows. {@code callState} is none, calling, ringing or in_call. */
public record PhoneDataPacket(boolean open, int battery, int signal, List<Contact> contacts, List<Line> messages,
                              String callState, UUID peer, String peerName, boolean voice) {
    public record Contact(UUID id, String name, boolean reachable) {}

    public record Line(UUID from, String fromName, UUID to, String text) {}

    static void encode(PhoneDataPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.open);
        buf.writeVarInt(p.battery);
        buf.writeVarInt(p.signal);
        buf.writeVarInt(p.contacts.size());
        for (Contact c : p.contacts) {
            buf.writeUUID(c.id);
            buf.writeUtf(c.name, 32);
            buf.writeBoolean(c.reachable);
        }
        buf.writeVarInt(p.messages.size());
        for (Line m : p.messages) {
            buf.writeUUID(m.from);
            buf.writeUtf(m.fromName, 32);
            buf.writeUUID(m.to);
            buf.writeUtf(m.text, 400);
        }
        buf.writeUtf(p.callState, 16);
        buf.writeUUID(p.peer);
        buf.writeUtf(p.peerName, 32);
        buf.writeBoolean(p.voice);
    }

    static PhoneDataPacket decode(FriendlyByteBuf buf) {
        boolean open = buf.readBoolean();
        int battery = buf.readVarInt();
        int signal = buf.readVarInt();
        List<Contact> contacts = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) contacts.add(new Contact(buf.readUUID(), buf.readUtf(32), buf.readBoolean()));
        List<Line> messages = new ArrayList<>();
        for (int i = buf.readVarInt(); i > 0; i--) messages.add(new Line(buf.readUUID(), buf.readUtf(32), buf.readUUID(), buf.readUtf(400)));
        return new PhoneDataPacket(open, battery, signal, contacts, messages, buf.readUtf(16), buf.readUUID(), buf.readUtf(32), buf.readBoolean());
    }

    static void handle(PhoneDataPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.triassicisland.client.ClientPacketHooks.phone(p));
        ctx.get().setPacketHandled(true);
    }
}
