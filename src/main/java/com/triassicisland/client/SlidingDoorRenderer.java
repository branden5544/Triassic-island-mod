package com.triassicisland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.triassicisland.TriassicIsland;
import com.triassicisland.smart.SlidingDoorBlock;
import com.triassicisland.smart.SlidingDoorBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Draws the glass panel of a sliding door, sliding sideways (toward the hinge side) as it opens. */
public class SlidingDoorRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity> {
    private static final ResourceLocation TEXTURE = TriassicIsland.id("textures/block/sliding_door_glass.png");

    private final ModelPart panel;

    public SlidingDoorRenderer(BlockEntityRendererProvider.Context context) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("panel", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5f, 0, -1, 15, 32, 2), PartPose.ZERO);
        this.panel = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("panel");
    }

    @Override
    public void render(SlidingDoorBlockEntity door, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = door.getBlockState();
        if (!(state.getBlock() instanceof SlidingDoorBlock)) return;
        Direction facing = state.getValue(DoorBlock.FACING);
        float slide = door.slide(partialTick);
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        // Local x runs across the doorway.
        pose.mulPose(Axis.YP.rotationDegrees(-facing.getClockWise().toYRot() + 90));
        pose.translate(slide * 14 / 16f * SlidingDoorBlock.slideDirection(state), 0, 0);
        panel.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(SlidingDoorBlockEntity door) {
        return false;
    }
}
