package com.triassicisland.client;

import com.triassicisland.TriassicIsland;
import com.triassicisland.machine.FossilCleanerBlockEntity;
import com.triassicisland.machine.MachineKind;
import com.triassicisland.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Draws every machine from one texture: a blank panel plus sprites for slots, the progress arrow and the flame.
 * Slot positions come from {@link MachineKind}, so new machines need no new textures.
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation TEXTURE = TriassicIsland.id("textures/gui/machine.png");
    private static final int ARROW_X = 79, ARROW_Y = 34, ARROW_W = 24, ARROW_H = 17;
    private static final int FLAME_X = 81, FLAME_Y = 33, FLAME_SIZE = 14;
    private static final int TANK_X = 20, TANK_Y = 15, TANK_W = 16, TANK_H = 34;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        for (MachineKind.SlotDef slot : menu.kind().slots) {
            g.blit(TEXTURE, x + slot.x() - 1, y + slot.y() - 1, 176, 0, 18, 18);
        }

        if (menu.kind().isCharger()) {
            // Average charge of the items inside.
            int bx = x + 53 - 1, by = y + 52, bw = 72;
            g.fill(bx - 1, by - 1, bx + bw + 1, by + 7, 0xFF373737);
            g.fill(bx, by, bx + bw, by + 6, 0xFF8B8B8B);
            g.fill(bx, by, bx + Math.round(menu.progress() * bw), by + 6, 0xFF4CC24C);
        } else if (menu.kind().isGenerator()) {
            // Flame shrinks from the top as the current fuel item burns down.
            g.blit(TEXTURE, x + FLAME_X, y + FLAME_Y, 176, 35, FLAME_SIZE, FLAME_SIZE);
            int lit = Math.round(menu.progress() * FLAME_SIZE);
            g.blit(TEXTURE, x + FLAME_X, y + FLAME_Y + FLAME_SIZE - lit, 190, 35 + FLAME_SIZE - lit, FLAME_SIZE, lit);
        } else {
            g.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 176, 18, ARROW_W, ARROW_H);
            g.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, 200, 18, Math.round(menu.progress() * ARROW_W), ARROW_H);
        }

        if (menu.kind().hasWaterTank()) {
            int tx = x + TANK_X, ty = y + TANK_Y;
            g.fill(tx - 1, ty - 1, tx + TANK_W + 1, ty + TANK_H + 1, 0xFF373737);
            g.fill(tx, ty, tx + TANK_W, ty + TANK_H, 0xFF8B8B8B);
            int filled = TANK_H * menu.tank() / FossilCleanerBlockEntity.TANK_CAPACITY;
            g.fill(tx, ty + TANK_H - filled, tx + TANK_W, ty + TANK_H, 0xFF3F76E4);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        int watts = menu.watts();
        boolean powered = menu.kind().isGenerator() ? menu.decivolts() > 0 : watts > 0;
        Component reading = Component.literal(String.format("%.1f V  %d W", menu.decivolts() / 10f, watts));
        int color = powered ? 0x2E7D32 : 0xB71C1C;
        g.drawString(font, reading, imageWidth - 8 - font.width(reading), inventoryLabelY, color, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (menu.kind().hasWaterTank() && isHovering(TANK_X, TANK_Y, TANK_W, TANK_H, mouseX, mouseY)) {
            g.renderTooltip(font, Component.translatable("gui.triassicisland.water", menu.tank(), FossilCleanerBlockEntity.TANK_CAPACITY), mouseX, mouseY);
        }
        if (!menu.kind().isGenerator() && !menu.kind().isCharger() && menu.watts() == 0 && isHovering(ARROW_X, ARROW_Y, ARROW_W, ARROW_H, mouseX, mouseY)) {
            g.renderTooltip(font, Component.translatable("gui.triassicisland.needs_power", menu.kind().watts), mouseX, mouseY);
        }
    }
}
