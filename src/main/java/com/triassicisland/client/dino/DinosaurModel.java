package com.triassicisland.client.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinosaurEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generated dinosaur model with procedural animation: walking legs (pairs for bipeds, diagonals for
 * quadrupeds), a swaying tail, a head that looks around, and a jaw that opens when it attacks.
 */
public class DinosaurModel extends EntityModel<DinosaurEntity> {
    private final ModelPart root;
    private final Map<String, ModelPart> parts = new HashMap<>();
    private final DinoSpec spec;

    DinosaurModel(ModelPart root, List<String> paths, DinoSpec spec) {
        this.root = root;
        this.spec = spec;
        for (String path : paths) {
            ModelPart part = root;
            for (String name : path.split("/")) part = part.getChild(name);
            parts.put(path.substring(path.lastIndexOf('/') + 1), part);
        }
    }

    private ModelPart part(String name) {
        return parts.get(name);
    }

    @Override
    public void setupAnim(DinosaurEntity dino, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        // Bigger animals take slower strides for the same ground speed.
        float size = Math.max(1, spec.scale() * dino.growthScale());
        float walk = limbSwing * 0.7f / (float) Math.sqrt(size);
        float amount = Math.min(1, limbSwingAmount * 1.5f);
        float yaw = netHeadYaw * Mth.DEG_TO_RAD, pitch = headPitch * Mth.DEG_TO_RAD;

        switch (spec.body()) {
            case THEROPOD, ORNITHOPOD -> {
                swing("leg_l", Mth.cos(walk) * 0.8f * amount);
                swing("leg_r", -Mth.cos(walk) * 0.8f * amount);
                swing("arm_l", Mth.cos(walk + 1) * 0.2f * amount);
                swing("arm_r", -Mth.cos(walk + 1) * 0.2f * amount);
                ModelPart body = part("body");
                body.y += Mth.abs(Mth.sin(walk)) * -0.6f * amount + Mth.sin(ageInTicks * 0.08f) * 0.15f;
            }
            case PTEROSAUR -> {
                boolean flying = dino instanceof com.triassicisland.dino.FlyingDinosaurEntity flyer && flyer.isFlying();
                for (int side = -1; side <= 1; side += 2) {
                    String s = side < 0 ? "r" : "l";
                    ModelPart wing = part("wing_" + s), tip = part("wing_tip_" + s);
                    if (flying) {
                        float flap = Mth.sin(ageInTicks * 0.35f);
                        wing.zRot += side * flap * 0.55f;
                        tip.zRot += side * Mth.sin(ageInTicks * 0.35f - 0.6f) * 0.3f;
                    } else {
                        // Folded along the body.
                        wing.zRot += side * 1.25f;
                        wing.yRot += -side * 0.9f;
                        tip.zRot += -side * 2.6f;
                    }
                }
                if (flying) {
                    swing("leg_l", 1.2f);
                    swing("leg_r", 1.2f);
                } else {
                    swing("leg_l", Mth.cos(walk) * 0.8f * amount);
                    swing("leg_r", -Mth.cos(walk) * 0.8f * amount);
                }
            }
            case MARINE -> {
                float speed = 1 + amount * 2;
                ModelPart body = part("body");
                body.yRot += Mth.sin(ageInTicks * 0.15f) * 0.06f * speed;
                for (String f : new String[]{"flipper_fl", "flipper_bl"}) {
                    ModelPart p = part(f);
                    if (p != null) p.zRot += Mth.sin(ageInTicks * 0.2f) * 0.4f;
                }
                for (String f : new String[]{"flipper_fr", "flipper_br"}) {
                    ModelPart p = part(f);
                    if (p != null) p.zRot -= Mth.sin(ageInTicks * 0.2f) * 0.4f;
                }
                turn("neck", Mth.sin(ageInTicks * 0.1f) * 0.15f);
            }
            case SAUROPOD, QUADRUPED -> {
                float stride = spec.body() == DinoSpec.Body.SAUROPOD ? 0.45f : 0.7f;
                swing("leg_fl", Mth.cos(walk) * stride * amount);
                swing("leg_br", Mth.cos(walk) * stride * amount);
                swing("leg_fr", -Mth.cos(walk) * stride * amount);
                swing("leg_bl", -Mth.cos(walk) * stride * amount);
            }
        }

        // Tail sway: idle breathing plus a swing in time with the legs (a strong beat for swimmers).
        float sway = spec.body() == DinoSpec.Body.MARINE
                ? Mth.sin(ageInTicks * 0.2f) * (0.2f + 0.25f * Math.min(1, limbSwingAmount * 2))
                : Mth.sin(ageInTicks * 0.07f) * 0.12f + Mth.cos(walk) * 0.15f * amount;
        turn("tail", sway);
        turn("tail2", sway * 1.3f);
        turn("tail3", sway * 1.6f);

        // Look: split between neck and head.
        ModelPart neck = part("neck"), head = part("head");
        if (neck != null) neck.yRot += yaw * 0.4f;
        if (head != null) {
            head.yRot += yaw * (neck != null ? 0.5f : 0.9f);
            head.xRot += pitch * 0.5f;
        }
        ModelPart jaw = part("jaw");
        if (jaw != null) jaw.xRot += attackTime > 0 ? Mth.sin(attackTime * Mth.PI) * 0.7f : Math.max(0, Mth.sin(ageInTicks * 0.02f) - 0.95f) * 6;
    }

    private void swing(String name, float angle) {
        ModelPart part = part(name);
        if (part != null) part.xRot += angle;
    }

    private void turn(String name, float angle) {
        ModelPart part = part(name);
        if (part != null) part.yRot += angle;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
