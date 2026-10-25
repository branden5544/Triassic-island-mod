package com.triassicisland.phone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Every phone message sent in this world (the last 1000), so conversations survive restarts. */
public class PhoneMessages extends SavedData {
    private static final int MAX = 1000;

    public record Message(UUID from, String fromName, UUID to, String toName, String text, long time) {}

    private final List<Message> messages = new ArrayList<>();

    public static PhoneMessages get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PhoneMessages::load, PhoneMessages::new, "triassicisland_phone");
    }

    public void add(Message message) {
        messages.add(message);
        while (messages.size() > MAX) messages.remove(0);
        setDirty();
    }

    /** The latest {@code limit} messages this player sent or received, oldest first. */
    public List<Message> involving(UUID player, int limit) {
        List<Message> out = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0 && out.size() < limit; i--) {
            Message m = messages.get(i);
            if (m.from.equals(player) || m.to.equals(player)) out.add(0, m);
        }
        return out;
    }

    private static PhoneMessages load(CompoundTag tag) {
        PhoneMessages data = new PhoneMessages();
        ListTag list = tag.getList("Messages", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag m = list.getCompound(i);
            data.messages.add(new Message(m.getUUID("From"), m.getString("FromName"), m.getUUID("To"), m.getString("ToName"),
                    m.getString("Text"), m.getLong("Time")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Message m : messages) {
            CompoundTag t = new CompoundTag();
            t.putUUID("From", m.from);
            t.putString("FromName", m.fromName);
            t.putUUID("To", m.to);
            t.putString("ToName", m.toName);
            t.putString("Text", m.text);
            t.putLong("Time", m.time);
            list.add(t);
        }
        tag.put("Messages", list);
        return tag;
    }
}
