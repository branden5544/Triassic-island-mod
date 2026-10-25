package com.triassicisland.phone;

import com.mojang.brigadier.CommandDispatcher;
import com.triassicisland.TriassicIsland;
import com.triassicisland.packet.ModNetwork;
import com.triassicisland.packet.PhoneDataPacket;
import com.triassicisland.smart.Wifi;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server side of the phones: messages, ringing, and calls. A player is reachable if they're online, carry a
 * charged phone, and are within range of a powered WiFi router. Calls need Simple Voice Chat ({@link VoiceBridge}); they ring for 30 seconds and use battery.
 */
@Mod.EventBusSubscriber(modid = TriassicIsland.MODID)
public final class PhoneService {
    public static final int MESSAGE_COST = 5;
    public static final int MAX_TEXT = 200;
    private static final int CALL_COST_PER_SECOND = 1;
    private static final int RING_SECONDS = 30;

    /** Callee -> caller, with the tick the call started ringing. */
    private record Ring(UUID caller, long since) {}

    private static final Map<UUID, Ring> RINGING = new HashMap<>();
    private static final Map<UUID, UUID> IN_CALL = new HashMap<>();
    private static final Set<UUID> SCREEN_OPEN = new HashSet<>();

    /** The phone the player would use: main hand, off hand, then anywhere in the inventory. Empty if none is charged. */
    public static ItemStack phoneOf(Player player) {
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (stack.getItem() instanceof PhoneItem phone && phone.getCharge(stack) > 0) return stack;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PhoneItem phone && phone.getCharge(stack) > 0) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Can this player use a phone right now: a charged phone and WiFi signal. */
    public static boolean reachable(ServerPlayer player) {
        return !phoneOf(player).isEmpty() && Wifi.signal(player) > 0;
    }

    private static MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    private static ServerPlayer online(UUID id) {
        return server() == null ? null : server().getPlayerList().getPlayer(id);
    }

    private static void tell(ServerPlayer player, MutableComponent message) {
        player.displayClientMessage(message, false);
    }

    private static MutableComponent prefix() {
        return Component.literal("[Phone] ").withStyle(ChatFormatting.AQUA);
    }

    // --- Screen ---

    public static void open(ServerPlayer player) {
        SCREEN_OPEN.add(player.getUUID());
        sendData(player, true);
    }

    static void sendData(ServerPlayer player, boolean openScreen) {
        ItemStack phone = phoneOf(player);
        int battery = phone.isEmpty() ? 0 : Math.round(100f * ((PhoneItem) phone.getItem()).getCharge(phone) / PhoneItem.CAPACITY);
        List<PhoneDataPacket.Contact> contacts = new ArrayList<>();
        for (ServerPlayer other : server().getPlayerList().getPlayers()) {
            if (other == player) continue;
            contacts.add(new PhoneDataPacket.Contact(other.getUUID(), other.getGameProfile().getName(), reachable(other)));
        }
        List<PhoneDataPacket.Line> lines = new ArrayList<>();
        for (PhoneMessages.Message m : PhoneMessages.get(server()).involving(player.getUUID(), 60)) {
            lines.add(new PhoneDataPacket.Line(m.from(), m.fromName(), m.to(), m.text()));
        }
        String state = "none";
        UUID peer = null;
        if (IN_CALL.containsKey(player.getUUID())) {
            state = "in_call";
            peer = IN_CALL.get(player.getUUID());
        } else if (RINGING.containsKey(player.getUUID())) {
            state = "ringing";
            peer = RINGING.get(player.getUUID()).caller();
        } else {
            for (var e : RINGING.entrySet()) {
                if (e.getValue().caller().equals(player.getUUID())) {
                    state = "calling";
                    peer = e.getKey();
                }
            }
        }
        ServerPlayer peerPlayer = peer == null ? null : online(peer);
        ModNetwork.toPlayer(player, new PhoneDataPacket(openScreen, battery, Wifi.signal(player), contacts, lines, state,
                peer == null ? new UUID(0, 0) : peer, peerPlayer == null ? "" : peerPlayer.getGameProfile().getName(),
                VoiceBridge.Holder.bridge != null));
    }

    private static void refreshIfOpen(ServerPlayer player) {
        if (player != null && SCREEN_OPEN.contains(player.getUUID())) sendData(player, false);
    }

    // --- Actions from the phone screen ---

    public static void handle(ServerPlayer player, String action, UUID target, String text) {
        switch (action) {
            case "refresh" -> sendData(player, false);
            case "close" -> SCREEN_OPEN.remove(player.getUUID());
            case "send" -> send(player, target, text);
            case "call" -> call(player, target);
            case "answer" -> answer(player);
            case "decline" -> decline(player);
            case "hangup" -> hangUp(player);
            default -> {}
        }
        if (!action.equals("close") && !action.equals("refresh")) refreshIfOpen(player);
    }

    private static boolean charged(ServerPlayer player, int cost) {
        if (Wifi.signal(player) == 0) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.no_signal").withStyle(ChatFormatting.RED)));
            return false;
        }
        ItemStack phone = phoneOf(player);
        if (phone.isEmpty() || !((PhoneItem) phone.getItem()).use(phone, cost)) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.flat").withStyle(ChatFormatting.RED)));
            return false;
        }
        return true;
    }

    private static void send(ServerPlayer player, UUID target, String text) {
        text = text.strip();
        if (text.isEmpty()) return;
        if (text.length() > MAX_TEXT) text = text.substring(0, MAX_TEXT);
        ServerPlayer to = online(target);
        if (to == null || !reachable(to)) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.unreachable")));
            return;
        }
        if (!charged(player, MESSAGE_COST)) return;
        String name = player.getGameProfile().getName();
        PhoneMessages.get(server()).add(new PhoneMessages.Message(player.getUUID(), name, to.getUUID(), to.getGameProfile().getName(),
                text, System.currentTimeMillis()));
        tell(to, prefix().append(Component.literal(name + ": ").withStyle(ChatFormatting.YELLOW)).append(Component.literal(text)));
        to.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.PLAYERS, 0.8f, 1.6f);
        refreshIfOpen(to);
    }

    private static void call(ServerPlayer player, UUID target) {
        VoiceBridge voice = VoiceBridge.Holder.bridge;
        if (voice == null) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.no_voice_mod")));
            return;
        }
        ServerPlayer to = online(target);
        if (to == null || !reachable(to)) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.unreachable")));
            return;
        }
        if (busy(player.getUUID()) || busy(to.getUUID())) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.busy")));
            return;
        }
        if (!voice.hasVoice(to.getUUID())) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.no_voice", to.getGameProfile().getName())));
            return;
        }
        if (!charged(player, CALL_COST_PER_SECOND)) return;
        RINGING.put(to.getUUID(), new Ring(player.getUUID(), server().getTickCount()));
        tell(player, prefix().append(Component.translatable("message.triassicisland.phone.calling", to.getGameProfile().getName())));
        tell(to, prefix().append(Component.translatable("message.triassicisland.phone.incoming", player.getGameProfile().getName()))
                .append(" ").append(button("answer", ChatFormatting.GREEN)).append(" ").append(button("decline", ChatFormatting.RED)));
        refreshIfOpen(to);
    }

    private static MutableComponent button(String action, ChatFormatting color) {
        return Component.translatable("message.triassicisland.phone.button." + action)
                .withStyle(s -> s.withColor(color).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phone " + action)));
    }

    private static boolean busy(UUID player) {
        if (IN_CALL.containsKey(player) || RINGING.containsKey(player)) return true;
        return RINGING.values().stream().anyMatch(r -> r.caller().equals(player));
    }

    private static void answer(ServerPlayer player) {
        Ring ring = RINGING.remove(player.getUUID());
        VoiceBridge voice = VoiceBridge.Holder.bridge;
        ServerPlayer caller = ring == null ? null : online(ring.caller());
        if (caller == null || voice == null) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.no_call")));
            return;
        }
        if (!voice.startCall(caller.getUUID(), player.getUUID())) {
            tell(player, prefix().append(Component.translatable("message.triassicisland.phone.call_failed")));
            tell(caller, prefix().append(Component.translatable("message.triassicisland.phone.call_failed")));
            return;
        }
        IN_CALL.put(caller.getUUID(), player.getUUID());
        IN_CALL.put(player.getUUID(), caller.getUUID());
        MutableComponent connected = prefix().append(Component.translatable("message.triassicisland.phone.connected"))
                .append(" ").append(button("hangup", ChatFormatting.RED));
        tell(player, connected.copy());
        tell(caller, connected);
        refreshIfOpen(caller);
    }

    private static void decline(ServerPlayer player) {
        Ring ring = RINGING.remove(player.getUUID());
        if (ring == null) return;
        ServerPlayer caller = online(ring.caller());
        if (caller != null) {
            tell(caller, prefix().append(Component.translatable("message.triassicisland.phone.declined", player.getGameProfile().getName())));
            refreshIfOpen(caller);
        }
    }

    /** Ends a call, or cancels a call this player is making or receiving. */
    static void hangUp(ServerPlayer player) {
        UUID id = player.getUUID();
        if (RINGING.containsKey(id)) {
            decline(player);
            return;
        }
        RINGING.entrySet().removeIf(e -> {
            if (!e.getValue().caller().equals(id)) return false;
            refreshIfOpen(online(e.getKey()));
            return true;
        });
        UUID peer = IN_CALL.remove(id);
        if (peer == null) return;
        IN_CALL.remove(peer);
        VoiceBridge voice = VoiceBridge.Holder.bridge;
        if (voice != null) voice.endCall(id, peer);
        tell(player, prefix().append(Component.translatable("message.triassicisland.phone.ended")));
        ServerPlayer other = online(peer);
        if (other != null) {
            tell(other, prefix().append(Component.translatable("message.triassicisland.phone.ended")));
            refreshIfOpen(other);
        }
    }

    // --- Ringing, battery drain, cleanup ---

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || server() == null || server().getTickCount() % 20 != 0) return;
        long now = server().getTickCount();
        for (var e : List.copyOf(RINGING.entrySet())) {
            ServerPlayer callee = online(e.getKey()), caller = online(e.getValue().caller());
            if (callee == null || caller == null || now - e.getValue().since() > RING_SECONDS * 20L) {
                RINGING.remove(e.getKey());
                if (caller != null) {
                    tell(caller, prefix().append(Component.translatable("message.triassicisland.phone.no_answer")));
                    refreshIfOpen(caller);
                }
                if (callee != null) refreshIfOpen(callee);
                continue;
            }
            if (Wifi.signal(callee) == 0) {
                RINGING.remove(e.getKey());
                tell(caller, prefix().append(Component.translatable("message.triassicisland.phone.signal_lost")));
                refreshIfOpen(caller);
                continue;
            }
            callee.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 0.9f, 1.3f);
        }
        for (UUID id : List.copyOf(IN_CALL.keySet())) {
            ServerPlayer player = online(id);
            if (player == null) {
                endFor(id);
                continue;
            }
            ItemStack phone = phoneOf(player);
            if (Wifi.signal(player) == 0) {
                tell(player, prefix().append(Component.translatable("message.triassicisland.phone.signal_lost")));
                hangUp(player);
            } else if (phone.isEmpty() || !((PhoneItem) phone.getItem()).use(phone, CALL_COST_PER_SECOND)) {
                hangUp(player);
            }
        }
    }

    private static void endFor(UUID id) {
        UUID peer = IN_CALL.remove(id);
        if (peer == null) return;
        IN_CALL.remove(peer);
        VoiceBridge voice = VoiceBridge.Holder.bridge;
        if (voice != null) voice.endCall(id, peer);
        ServerPlayer other = online(peer);
        if (other != null) tell(other, prefix().append(Component.translatable("message.triassicisland.phone.ended")));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        SCREEN_OPEN.remove(id);
        RINGING.remove(id);
        RINGING.entrySet().removeIf(e -> e.getValue().caller().equals(id));
        endFor(id);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        for (String action : new String[]{"answer", "decline", "hangup"}) {
            dispatcher.register(Commands.literal("phone").then(Commands.literal(action).executes(ctx -> {
                handle(ctx.getSource().getPlayerOrException(), action, new UUID(0, 0), "");
                return 1;
            })));
        }
    }

    private PhoneService() {}
}
