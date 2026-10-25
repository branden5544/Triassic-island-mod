package com.triassicisland.client.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.triassicisland.TriassicIsland;
import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinosaurEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/** Renders one species: builds its model, paints its skin into a texture, and scales it with age. */
public class DinosaurRenderer extends MobRenderer<DinosaurEntity, DinosaurModel> {
    private final DinoSpec spec;
    private final ResourceLocation texture;

    public DinosaurRenderer(EntityRendererProvider.Context context, DinoSpec spec) {
        this(context, spec, DinoModelBuilder.build(spec));
    }

    private DinosaurRenderer(EntityRendererProvider.Context context, DinoSpec spec, DinoModelBuilder.Result built) {
        super(context, new DinosaurModel(built.layer().bakeRoot(), built.paths(), spec), 0.4f * spec.width());
        this.spec = spec;
        this.texture = TriassicIsland.id("dynamic/dinosaur/" + spec.species().id());
        Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(DinoSkin.paint(built, spec)));
    }

    @Override
    public ResourceLocation getTextureLocation(DinosaurEntity dino) {
        return texture;
    }

    @Override
    protected void scale(DinosaurEntity dino, PoseStack pose, float partialTick) {
        float s = spec.scale() * dino.growthScale();
        pose.scale(s, s, s);
        shadowRadius = 0.4f * spec.width() * dino.growthScale();
    }
}
