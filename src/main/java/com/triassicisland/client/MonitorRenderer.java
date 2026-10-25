package com.triassicisland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.triassicisland.security.MonitorBlockEntity;
import com.triassicisland.security.WallDeviceBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/** Draws a monitor's text on its screen, glowing (full brightness) while powered. */
public class MonitorRenderer implements BlockEntityRenderer<MonitorBlockEntity> {
    private static final float SCREEN_WIDTH = 14 / 16f;
    /** Front face of the monitor, measured from the block centre toward the viewer (it is 3 px deep from the wall). */
    private static final float FRONT = -0.5f + 3 / 16f + 0.004f;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Font font;

    public MonitorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(MonitorBlockEntity monitor, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!monitor.clientPowered()) return;
        String[] lines = monitor.clientLines();
        float scale = SCREEN_WIDTH / (MonitorBlockEntity.COLUMNS * 6f);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-monitor.getBlockState().getValue(WallDeviceBlock.FACING).toYRot()));
        pose.translate(-SCREEN_WIDTH / 2, MonitorBlockEntity.ROWS * 9 * scale / 2, FRONT);
        pose.scale(scale, -scale, scale);
        Matrix4f matrix = pose.last().pose();
        int color = 0xFF000000 | monitor.clientColor();
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c == ' ') continue;
                String s = String.valueOf(c);
                font.drawInBatch(s, col * 6 + (6 - font.width(s)) / 2f, row * 9, color, false, matrix, buffers,
                        Font.DisplayMode.POLYGON_OFFSET, 0, FULL_BRIGHT);
            }
        }
        pose.popPose();
    }
}
