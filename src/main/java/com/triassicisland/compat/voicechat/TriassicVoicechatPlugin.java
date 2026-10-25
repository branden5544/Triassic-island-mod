package com.triassicisland.compat.voicechat;

import com.triassicisland.TriassicIsland;
import com.triassicisland.phone.VoiceBridge;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Voice Chat integration. Only Simple Voice Chat loads this class (it looks for the annotation), so the mod
 * runs fine without it. A phone call is a hidden, isolated voice group with just the two callers.
 */
@ForgeVoicechatPlugin
public class TriassicVoicechatPlugin implements VoicechatPlugin, VoiceBridge {
    private volatile VoicechatServerApi api;
    private final Map<UUID, Group> calls = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return TriassicIsland.MODID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            api = event.getVoicechat();
            VoiceBridge.Holder.bridge = this;
            TriassicIsland.LOGGER.info("Simple Voice Chat found: phone calls enabled");
        });
        registration.registerEvent(VoicechatServerStoppedEvent.class, event -> {
            api = null;
            VoiceBridge.Holder.bridge = null;
            calls.clear();
        });
    }

    @Override
    public boolean hasVoice(UUID player) {
        VoicechatServerApi api = this.api;
        VoicechatConnection connection = api == null ? null : api.getConnectionOf(player);
        return connection != null && connection.isInstalled();
    }

    @Override
    public boolean startCall(UUID a, UUID b) {
        VoicechatServerApi api = this.api;
        if (api == null) return false;
        VoicechatConnection ca = api.getConnectionOf(a), cb = api.getConnectionOf(b);
        if (ca == null || cb == null) return false;
        Group group = api.groupBuilder()
                .setName("Phone call")
                .setPassword(UUID.randomUUID().toString())
                .setType(Group.Type.ISOLATED)
                .setHidden(true)
                .setPersistent(false)
                .build();
        ca.setGroup(group);
        cb.setGroup(group);
        calls.put(a, group);
        calls.put(b, group);
        return true;
    }

    @Override
    public void endCall(UUID a, UUID b) {
        VoicechatServerApi api = this.api;
        Group group = calls.remove(a);
        calls.remove(b);
        if (api == null || group == null) return;
        for (UUID id : new UUID[]{a, b}) {
            VoicechatConnection connection = api.getConnectionOf(id);
            if (connection != null && connection.getGroup() != null && connection.getGroup().getId().equals(group.getId())) {
                connection.setGroup(null);
            }
        }
        api.removeGroup(group.getId());
    }
}
