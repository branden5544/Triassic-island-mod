package com.triassicisland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.triassicisland.power.CableTier;
import com.triassicisland.power.PoleInsulatorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Draws overhead wires as sagging (catenary-like) ribbons. Each wire is drawn once, from its lower end. */
public class WireRenderer implements BlockEntityRenderer<PoleInsulatorBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/black_concrete.png");
    private static final int SEGMENTS = 20;
    private static final float THICKNESS = 0.035f;

    public WireRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PoleInsulatorBlockEntity pole, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockPos origin = pole.getBlockPos();
        VertexConsumer buffer = buffers.getBuffer(RenderType.entitySolid(TEXTURE));
        Matrix4f matrix = pose.last().pose();
        for (PoleInsulatorBlockEntity.Wire wire : pole.wires()) {
            if (origin.compareTo(wire.to()) > 0) continue;
            Vec3 base = Vec3.atLowerCornerOf(origin);
            Vec3 a = PoleInsulatorBlockEntity.anchor(origin).subtract(base);
            Vec3 b = PoleInsulatorBlockEntity.anchor(wire.to()).subtract(base);
            double sag = Math.min(2.5, a.distanceTo(b) * 0.06);
            int[] color = color(wire.tier());
            Vec3 previous = a;
            for (int i = 1; i <= SEGMENTS; i++) {
                double t = i / (double) SEGMENTS;
                Vec3 next = a.lerp(b, t).subtract(0, sag * 4 * t * (1 - t), 0);
                Vec3 mid = previous.add(next).scale(0.5).add(base);
                int packed = pole.getLevel() == null ? light : LevelRenderer.getLightColor(pole.getLevel(), BlockPos.containing(mid));
                ribbon(buffer, matrix, previous, next, color, packed, true);
                ribbon(buffer, matrix, previous, next, color, packed, false);
                previous = next;
            }
        }
    }

    private static int[] color(CableTier tier) {
        return switch (tier) {
            case LOW -> new int[]{60, 45, 40};
            case MEDIUM -> new int[]{150, 40, 35};
            case HIGH -> new int[]{170, 150, 40};
        };
    }

    /** One flat strip between two points, either horizontal or vertical, double-sided. */
    private static void ribbon(VertexConsumer buffer, Matrix4f matrix, Vec3 from, Vec3 to, int[] c, int light, boolean flat) {
        Vec3 dir = to.subtract(from).normalize();
        Vec3 side = flat ? dir.cross(new Vec3(0, 1, 0)) : dir.cross(new Vec3(1, 0, 0)).add(dir.cross(new Vec3(0, 0, 1)));
        if (side.lengthSqr() < 1e-6) side = new Vec3(1, 0, 0);
        side = side.normalize().scale(THICKNESS);
        Vec3[] quad = {from.add(side), to.add(side), to.subtract(side), from.subtract(side)};
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < 4; k++) {
                Vec3 v = quad[pass == 0 ? k : 3 - k];
                buffer.vertex(matrix, (float) v.x, (float) v.y, (float) v.z).color(c[0], c[1], c[2], 255)
                        .uv(k < 2 ? 0 : 1, k % 3 == 0 ? 0 : 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                        .normal(0, 1, 0).endVertex();
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(PoleInsulatorBlockEntity pole) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
