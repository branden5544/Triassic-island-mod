package com.triassicisland.client;

import com.triassicisland.packet.EditorOpenPacket;
import com.triassicisland.packet.EditorSavePacket;
import com.triassicisland.packet.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.charset.StandardCharsets;

/** Full-screen text editor for a computer file. Tab inserts 4 spaces (Python cares); Ctrl+S saves. */
public class EditorScreen extends Screen {
    private final BlockPos pos;
    private final String name;
    private final boolean readOnly;
    private String text;
    private MultiLineEditBox box;
    private Component status = Component.empty();

    public EditorScreen(BlockPos pos, String name, String text) {
        super(Component.translatable("gui.triassicisland.editor.title", name));
        this.pos = pos;
        this.name = name;
        this.text = text;
        this.readOnly = name.startsWith("rom/");
    }

    @Override
    protected void init() {
        if (box != null) text = box.getValue();
        box = new MultiLineEditBox(font, 10, 22, width - 20, height - 56, Component.empty(), Component.literal(name));
        box.setCharacterLimit(EditorOpenPacket.MAX_BYTES);
        box.setValue(text);
        addRenderableWidget(box);
        setInitialFocus(box);

        int y = height - 28;
        Button save = addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.editor.save"), b -> save())
                .bounds(width / 2 - 104, y, 100, 20).build());
        save.active = !readOnly;
        addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.editor.close"), b -> onClose())
                .bounds(width / 2 + 4, y, 100, 20).build());
        if (readOnly) status = Component.translatable("gui.triassicisland.editor.read_only");
    }

    private void save() {
        if (readOnly) return;
        String value = box.getValue();
        if (value.getBytes(StandardCharsets.UTF_8).length > EditorOpenPacket.MAX_BYTES) {
            status = Component.translatable("gui.triassicisland.editor.too_big");
            return;
        }
        ModNetwork.toServer(new EditorSavePacket(pos, name, value));
        status = Component.translatable("gui.triassicisland.editor.saved");
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_TAB && box.isFocused()) {
            for (int i = 0; i < 4; i++) box.charTyped(' ', 0);
            return true;
        }
        if (key == GLFW.GLFW_KEY_S && hasControlDown()) {
            save();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawString(font, title, 10, 8, 0xFFFFFF);
        g.drawString(font, status, width - 10 - font.width(status), 8, 0xA0A0A0);
        super.render(g, mouseX, mouseY, partialTick);
    }

    /** Back to the terminal when done. */
    @Override
    public void onClose() {
        TerminalScreen terminal = new TerminalScreen(pos);
        minecraft.setScreen(terminal);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
