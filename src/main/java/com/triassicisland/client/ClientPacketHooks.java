package com.triassicisland.client;

import com.triassicisland.computer.Terminal;
import com.triassicisland.packet.CameraViewPacket;
import com.triassicisland.packet.EditorOpenPacket;
import com.triassicisland.packet.PhoneDataPacket;
import com.triassicisland.packet.TerminalUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Client-only packet handling, kept out of the packet classes so dedicated servers never load it. */
public final class ClientPacketHooks {
    /** Latest screen contents per computer. */
    static final Map<BlockPos, TerminalUpdatePacket> TERMINALS = new HashMap<>();

    public static void openTerminal(BlockPos pos) {
        Minecraft.getInstance().setScreen(new TerminalScreen(pos));
    }

    public static void updateTerminal(TerminalUpdatePacket packet) {
        TERMINALS.put(packet.pos(), packet);
    }

    public static void openEditor(EditorOpenPacket packet) {
        if (Minecraft.getInstance().screen instanceof TerminalScreen terminal) terminal.keepWatching = true;
        Minecraft.getInstance().setScreen(new EditorScreen(packet.pos(), packet.name(), packet.text()));
    }

    public static void viewCameras(CameraViewPacket packet) {
        if (packet.cameras().isEmpty()) return;
        if (Minecraft.getInstance().screen instanceof TerminalScreen terminal) terminal.keepWatching = true;
        Minecraft.getInstance().setScreen(new CameraViewScreen(packet));
    }

    public static void phone(PhoneDataPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PhoneScreen screen) {
            screen.update(packet);
        } else if (packet.open()) {
            mc.setScreen(new PhoneScreen(packet));
        }
    }

    static TerminalUpdatePacket terminal(BlockPos pos) {
        TerminalUpdatePacket packet = TERMINALS.get(pos);
        if (packet != null) return packet;
        String[] blank = new String[Terminal.HEIGHT];
        Arrays.fill(blank, "");
        return new TerminalUpdatePacket(pos, blank, 0, 0, false);
    }

    private ClientPacketHooks() {}
}
