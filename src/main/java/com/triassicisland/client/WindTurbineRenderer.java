package com.triassicisland.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.triassicisland.TriassicIsland;
import com.triassicisland.power.WindTurbineBlock;
import com.triassicisland.power.WindTurbineBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Draws the turbine's mast, nacelle and a three-blade rotor that spins with the wind. */
public class WindTurbineRenderer implements BlockEntityRenderer<WindTurbineBlockEntity> {
    private static final ResourceLocation TEXTURE = TriassicIsland.id("textures/block/wind_turbine_tower.png");
    private static final float HUB_Y = (WindTurbineBlock.HEIGHT - 0.5f) * 16;

    private final ModelPart tower;
    private final ModelPart rotor;

    public WindTurbineRenderer(BlockEntityRendererProvider.Context context) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Built y-up in pixels from the base block's floor; no flipping needed when rendering.
        root.addOrReplaceChild("tower", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5f, 0, -2.5f, 5, HUB_Y - 4, 5)
                .texOffs(0, 0).addBox(-3.5f, HUB_Y - 5, -6, 7, 6, 12), PartPose.ZERO);
        PartDefinition rotor = root.addOrReplaceChild("rotor", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2, -2, -2, 4, 4, 3), PartPose.offset(0, HUB_Y - 2, -6.5f));
        for (int i = 0; i < 3; i++) {
            rotor.addOrReplaceChild("blade" + i, CubeListBuilder.create()
                    .texOffs(32, 0).addBox(-1.5f, 1.5f, -0.5f, 3, 38, 1), PartPose.rotation(0, 0, i * (float) (Math.PI * 2 / 3)));
        }
        ModelPart baked = LayerDefinition.create(mesh, 64, 128).bakeRoot();
        this.tower = baked.getChild("tower");
        this.rotor = baked.getChild("rotor");
    }

    @Override
    public void render(WindTurbineBlockEntity turbine, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var level = turbine.getLevel();
        double wind = WindTurbineBlockEntity.windAt(level, turbine.getBlockPos());
        float time = level == null ? 0 : level.getGameTime() + partialTick;
        float angle = (float) (time * (0.02 + wind * 0.25));

        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        var buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        tower.render(pose, buffer, light, overlay);
        rotor.zRot = angle;
        rotor.render(pose, buffer, light, overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(WindTurbineBlockEntity turbine) {
        return true;
    }
}
