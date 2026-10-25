package com.triassicisland.client;

import com.triassicisland.packet.CameraViewPacket;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Looks through a security camera by moving the game's camera to an invisible marker at the lens.
 * A/D pan, W/S tilt, Left/Right switch cameras, Esc returns to the terminal.
 * Only areas the client has loaded (within render distance of the player) can be seen.
 */
public class CameraViewScreen extends Screen {
    private static final float MAX_PAN = 70, MIN_TILT = -30, MAX_TILT = 70, STEP = 3;

    private final CameraViewPacket view;
    private int index;
    private float pan, tilt = 25;
    private Marker eye;
    private boolean previousHideGui;
    private boolean restored;

    public CameraViewScreen(CameraViewPacket view) {
        super(Component.translatable("gui.triassicisland.camera"));
        this.view = view;
        this.index = view.start();
    }

    @Override
    protected void init() {
        if (eye == null) {
            previousHideGui = minecraft.options.hideGui;
            eye = new Marker(EntityType.MARKER, minecraft.level);
            aim();
        }
        minecraft.options.hideGui = true;
        minecraft.setCameraEntity(eye);
    }

    private CameraViewPacket.Camera camera() {
        return view.cameras().get(index);
    }

    private void aim() {
        CameraViewPacket.Camera camera = camera();
        float yaw = camera.yaw() + pan;
        BlockPos pos = camera.pos();
        // Sit just in front of the lens so the camera's own model doesn't block the view.
        Vec3 forward = Vec3.directionFromRotation(0, camera.yaw());
        Vec3 at = Vec3.atCenterOf(pos).add(forward.scale(0.45)).add(0, 0.05, 0);
        eye.moveTo(at.x, at.y, at.z, yaw, tilt);
        eye.xo = eye.xOld = at.x;
        eye.yo = eye.yOld = at.y;
        eye.zo = eye.zOld = at.z;
        eye.yRotO = yaw;
        eye.xRotO = tilt;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        switch (key) {
            case GLFW.GLFW_KEY_A -> pan = Mth.clamp(pan - STEP, -MAX_PAN, MAX_PAN);
            case GLFW.GLFW_KEY_D -> pan = Mth.clamp(pan + STEP, -MAX_PAN, MAX_PAN);
            case GLFW.GLFW_KEY_W -> tilt = Mth.clamp(tilt - STEP, MIN_TILT, MAX_TILT);
            case GLFW.GLFW_KEY_S -> tilt = Mth.clamp(tilt + STEP, MIN_TILT, MAX_TILT);
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> {
                int n = view.cameras().size();
                index = (index + (key == GLFW.GLFW_KEY_LEFT ? n - 1 : 1)) % n;
                pan = 0;
                tilt = 25;
            }
            default -> {
                return super.keyPressed(key, scanCode, modifiers);
            }
        }
        aim();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        CameraViewPacket.Camera camera = camera();
        if (!camera.powered()) {
            g.fill(0, 0, width, height, 0xFF000000);
            Component noSignal = Component.translatable("gui.triassicisland.camera.no_signal");
            g.drawString(font, noSignal, (width - font.width(noSignal)) / 2, height / 2, 0xFFFFFF);
        } else {
            // Faint scanlines.
            for (int y = 0; y < height; y += 3) g.fill(0, y, width, y + 1, 0x18000000);
        }
        boolean blink = (Util.getMillis() / 600) % 2 == 0;
        if (blink) g.fill(8, 9, 14, 15, 0xFFFF2020);
        String name = camera.label().isEmpty() ? camera.id() : camera.label() + " (" + camera.id() + ")";
        g.drawString(font, "REC  CAM " + (index + 1) + "/" + view.cameras().size() + "  " + name, 18, 8, 0xFFFFFF);
        long time = minecraft.level == null ? 0 : (minecraft.level.getDayTime() + 6000) % 24000;
        String clock = String.format("%02d:%02d", time / 1000, (time % 1000) * 60 / 1000);
        g.drawString(font, clock, width - 8 - font.width(clock), 8, 0xFFFFFF);
        Component help = Component.translatable("gui.triassicisland.camera.help");
        g.drawString(font, help, (width - font.width(help)) / 2, height - 14, 0xC0C0C0);
    }

    @Override
    public void renderBackground(GuiGraphics g) {}

    private void restore() {
        if (restored) return;
        restored = true;
        minecraft.options.hideGui = previousHideGui;
        Entity player = minecraft.player;
        minecraft.setCameraEntity(player);
    }

    @Override
    public void onClose() {
        restore();
        TerminalScreen terminal = new TerminalScreen(view.computer());
        minecraft.setScreen(terminal);
    }

    @Override
    public void removed() {
        restore();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
