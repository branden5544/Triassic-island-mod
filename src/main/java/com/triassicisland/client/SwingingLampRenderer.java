package com.triassicisland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.triassicisland.TriassicIsland;
import com.triassicisland.light.LampSocketBlock;
import com.triassicisland.light.LampSocketBlockEntity;
import com.triassicisland.light.SocketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/** Swinging suspended lamps: the chain and shade hang from the ceiling mount and sway gently. */
public class SwingingLampRenderer implements BlockEntityRenderer<LampSocketBlockEntity> {
    public SwingingLampRenderer(BlockEntityRendererProvider.Context context) {}

    /** Extra models: the hanging part of each swinging socket, without bulb / unlit / lit. */
    public static ResourceLocation hanging(SocketType type, String state) {
        return TriassicIsland.id("block/" + type.id() + "_hanging_" + state);
    }

    @Override
    public void render(LampSocketBlockEntity socket, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = socket.getBlockState();
        if (!(state.getBlock() instanceof LampSocketBlock block) || !block.type.swings) return;
        String look = !state.getValue(LampSocketBlock.HAS_BULB) ? "empty" : state.getValue(LampSocketBlock.LIGHT) > 0 ? "on" : "off";
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(hanging(block.type, look));

        float time = (socket.getLevel() == null ? 0 : socket.getLevel().getGameTime()) + partialTick;
        float phase = socket.getBlockPos().asLong() % 100;
        pose.pushPose();
        pose.translate(0.5, 1, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees((float) Math.sin(time * 0.045 + phase) * 6));
        pose.mulPose(Axis.ZP.rotationDegrees((float) Math.cos(time * 0.037 + phase) * 4));
        pose.translate(-0.5, -1, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(RenderType.cutout()),
                state, model, 1, 1, 1, state.getValue(LampSocketBlock.LIGHT) > 0 ? 0xF000F0 : light, overlay, ModelData.EMPTY, RenderType.cutout());
        pose.popPose();
    }
}
