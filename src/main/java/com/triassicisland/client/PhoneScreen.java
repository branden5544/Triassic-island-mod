package com.triassicisland.client;

import com.triassicisland.packet.ModNetwork;
import com.triassicisland.packet.PhoneActionPacket;
import com.triassicisland.packet.PhoneDataPacket;
import com.triassicisland.phone.PhoneService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The smartphone: contacts on the left, the conversation with the selected contact, a message box, and call buttons. */
public class PhoneScreen extends Screen {
    private static final int W = 300, H = 200;
    private static final int CONTACTS_W = 90;

    private PhoneDataPacket data;
    private UUID selected;
    private EditBox message;
    private int left, top;

    public PhoneScreen(PhoneDataPacket data) {
        super(Component.translatable("item.triassicisland.smartphone"));
        this.data = data;
        if (!data.contacts().isEmpty()) selected = data.contacts().get(0).id();
        if (!"none".equals(data.callState())) selected = data.peer();
    }

    void update(PhoneDataPacket data) {
        this.data = data;
        if (selected == null && !data.contacts().isEmpty()) selected = data.contacts().get(0).id();
        rebuildWidgets();
    }

    private void action(String action, String text) {
        ModNetwork.toServer(new PhoneActionPacket(action, selected == null ? new UUID(0, 0) : selected, text));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        String typed = message == null ? "" : message.getValue();

        // Contacts
        int y = top + 22;
        for (PhoneDataPacket.Contact contact : data.contacts()) {
            if (y > top + H - 30) break;
            Component label = Component.literal((contact.id().equals(selected) ? "> " : "") + contact.name());
            Button button = Button.builder(label, b -> {
                selected = contact.id();
                rebuildWidgets();
            }).bounds(left + 6, y, CONTACTS_W - 8, 16).build();
            button.active = contact.reachable() || contact.id().equals(selected);
            addRenderableWidget(button);
            y += 18;
        }

        // Message box
        int boxX = left + CONTACTS_W + 4, boxW = W - CONTACTS_W - 10;
        message = new EditBox(font, boxX, top + H - 46, boxW - 44, 16, Component.translatable("gui.triassicisland.phone.message"));
        message.setMaxLength(PhoneService.MAX_TEXT);
        message.setValue(typed);
        addRenderableWidget(message);
        Button send = addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.phone.send"), b -> send())
                .bounds(boxX + boxW - 42, top + H - 46, 42, 16).build());
        send.active = selected != null;

        // Call controls
        int callY = top + H - 26;
        switch (data.callState()) {
            case "ringing" -> {
                addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.phone.answer"), b -> action("answer", ""))
                        .bounds(boxX, callY, boxW / 2 - 2, 18).build());
                addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.phone.decline"), b -> action("decline", ""))
                        .bounds(boxX + boxW / 2 + 2, callY, boxW / 2 - 2, 18).build());
            }
            case "calling", "in_call" -> addRenderableWidget(Button.builder(Component.translatable("gui.triassicisland.phone.hangup"),
                    b -> action("hangup", "")).bounds(boxX, callY, boxW, 18).build());
            default -> {
                Button call = addRenderableWidget(Button.builder(Component.translatable(data.voice()
                        ? "gui.triassicisland.phone.call" : "gui.triassicisland.phone.call_no_voice"), b -> action("call", ""))
                        .bounds(boxX, callY, boxW, 18).build());
                call.active = selected != null;
            }
        }
        setInitialFocus(message);
    }

    private void send() {
        if (selected == null || message.getValue().isBlank()) return;
        action("send", message.getValue());
        message.setValue("");
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && message.isFocused()) {
            send();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        // Phone body
        g.fill(left - 4, top - 4, left + W + 4, top + H + 4, 0xFF15161A);
        g.fill(left, top, left + W, top + H, 0xFF22252B);
        g.fill(left, top, left + W, top + 16, 0xFF2F333B);
        g.drawString(font, title, left + 6, top + 4, 0xFFFFFF);
        String battery = data.battery() + "%";
        int batteryColor = data.battery() > 30 ? 0x7CFC7C : data.battery() > 10 ? 0xFFD040 : 0xFF5050;
        g.drawString(font, battery, left + W - 6 - font.width(battery), top + 4, batteryColor);
        // Signal bars
        int sx = left + W - 14 - font.width(battery) - 16;
        for (int i = 0; i < 4; i++) {
            int h = 3 + i * 2;
            g.fill(sx + i * 4, top + 12 - h, sx + i * 4 + 3, top + 12, i < data.signal() ? 0xFFE0E0E0 : 0xFF50545C);
        }
        if (data.signal() == 0) g.drawString(font, Component.translatable("gui.triassicisland.phone.no_signal"), sx - 60, top + 4, 0xFF5050);
        g.fill(left + CONTACTS_W, top + 16, left + CONTACTS_W + 1, top + H, 0xFF3A3F48);
        if (data.contacts().isEmpty()) g.drawString(font, Component.translatable("gui.triassicisland.phone.nobody"), left + 6, top + 24, 0x9AA0A8);

        // Call status
        String status = switch (data.callState()) {
            case "calling" -> Component.translatable("gui.triassicisland.phone.status.calling", data.peerName()).getString();
            case "ringing" -> Component.translatable("gui.triassicisland.phone.status.ringing", data.peerName()).getString();
            case "in_call" -> Component.translatable("gui.triassicisland.phone.status.in_call", data.peerName()).getString();
            default -> "";
        };
        int chatX = left + CONTACTS_W + 6, chatW = W - CONTACTS_W - 12;
        if (!status.isEmpty()) g.drawString(font, status, chatX, top + 20, 0x7CFC7C);

        // Conversation with the selected contact, newest at the bottom.
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (PhoneDataPacket.Line m : data.messages()) {
            if (selected == null || !(m.from().equals(selected) || m.to().equals(selected))) continue;
            boolean mine = !m.from().equals(selected);
            Component text = Component.literal((mine ? "You: " : m.fromName() + ": ") + m.text());
            for (FormattedCharSequence line : font.split(text, chatW)) lines.add(line);
        }
        int bottom = top + H - 52, rows = (bottom - (top + 32)) / 10;
        int start = Math.max(0, lines.size() - rows);
        for (int i = start; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), chatX, bottom - (lines.size() - i) * 10, 0xE0E0E0);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        action("close", "");
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
