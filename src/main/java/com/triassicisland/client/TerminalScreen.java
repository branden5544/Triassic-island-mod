package com.triassicisland.client;

import com.triassicisland.computer.Terminal;
import com.triassicisland.packet.ModNetwork;
import com.triassicisland.packet.TerminalActionPacket;
import com.triassicisland.packet.TerminalInputPacket;
import com.triassicisland.packet.TerminalUpdatePacket;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The computer's terminal: a fixed-width character grid drawn cell by cell (Minecraft's font is proportional),
 * an input line, and Terminate / Reboot / Power buttons. Ctrl+T also terminates; Up/Down recall earlier lines.
 */
public class TerminalScreen extends Screen {
    private static final int CELL_W = 6, CELL_H = 9, PAD = 6;
    private static final int TEXT = 0xFF33FF66, BACKGROUND = 0xFF0B110B, BORDER = 0xFF4A4F4A;
    private static final List<String> HISTORY = new ArrayList<>();

    private final BlockPos pos;
    private EditBox input;
    private int historyIndex;
    private int left, top, panelW, panelH;
    /** True when switching to the editor, so we don't tell the server we stopped watching. */
    boolean keepWatching;

    public TerminalScreen(BlockPos pos) {
        super(Component.translatable("block.triassicisland.computer"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        panelW = Terminal.WIDTH * CELL_W + PAD * 2;
        panelH = Terminal.HEIGHT * CELL_H + PAD * 2;
        left = (width - panelW) / 2;
        top = Math.max(2, (height - panelH - 48) / 2);

        String typed = input == null ? "" : input.getValue();
        input = new EditBox(font, left, top + panelH + 4, panelW, 18, Component.translatable("gui.triassicisland.terminal.input"));
        input.setMaxLength(TerminalInputPacket.MAX_LENGTH);
        input.setValue(typed);
        addRenderableWidget(input);
        setInitialFocus(input);

        int buttonW = (panelW - 6) / 4, y = top + panelH + 26;
        addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.terminal.terminate"), b -> action("terminate"))
                .bounds(left, y, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.terminal.reboot"), b -> action("reboot"))
                .bounds(left + (buttonW + 2), y, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.terminal.power"), b -> action("power"))
                .bounds(left + 2 * (buttonW + 2), y, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + 3 * (buttonW + 2), y, buttonW, 20).build());
    }

    private void action(String action) {
        ModNetwork.toServer(new TerminalActionPacket(pos, action));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 2, top - 2, left + panelW + 2, top + panelH + 2, BORDER);
        g.fill(left, top, left + panelW, top + panelH, BACKGROUND);

        TerminalUpdatePacket screen = ClientPacketHooks.terminal(pos);
        for (int row = 0; row < screen.lines().length; row++) {
            String line = screen.lines()[row];
            int y = top + PAD + row * CELL_H;
            for (int col = 0; col < line.length() && col < Terminal.WIDTH; col++) {
                char c = line.charAt(col);
                if (c == ' ') continue;
                String s = String.valueOf(c);
                g.drawString(font, s, left + PAD + col * CELL_W + (CELL_W - font.width(s)) / 2, y, TEXT, false);
            }
        }
        if (screen.running() && (Util.getMillis() / 500) % 2 == 0) {
            int x = left + PAD + screen.cursorX() * CELL_W, y = top + PAD + screen.cursorY() * CELL_H;
            g.fill(x, y + CELL_H - 2, x + CELL_W - 1, y + CELL_H - 1, TEXT);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_T && hasControlDown()) {
            action("terminate");
            return true;
        }
        if (input.isFocused()) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                String line = input.getValue();
                ModNetwork.toServer(new TerminalInputPacket(pos, line));
                if (!line.isBlank() && (HISTORY.isEmpty() || !HISTORY.get(HISTORY.size() - 1).equals(line))) HISTORY.add(line);
                historyIndex = HISTORY.size();
                input.setValue("");
                return true;
            }
            if (key == GLFW.GLFW_KEY_UP && historyIndex > 0) {
                input.setValue(HISTORY.get(--historyIndex));
                return true;
            }
            if (key == GLFW.GLFW_KEY_DOWN) {
                historyIndex = Math.min(HISTORY.size(), historyIndex + 1);
                input.setValue(historyIndex < HISTORY.size() ? HISTORY.get(historyIndex) : "");
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void removed() {
        if (!keepWatching) action("close");
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
