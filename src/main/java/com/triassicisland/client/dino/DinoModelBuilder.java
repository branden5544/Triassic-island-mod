package com.triassicisland.client.dino;

import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinoSpec.Body;
import com.triassicisland.dino.DinoSpec.Feature;
import com.triassicisland.dino.DinoSpec.Shape;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a species' model from its body plan, proportions and features, and remembers where every box sits
 * on the texture so {@link DinoSkin} can paint a matching skin. Models are built at a reference size
 * (hips about 1 block up) and scaled at render time. Model space: y points down, the ground is y = 24,
 * and the head points toward -z.
 */
final class DinoModelBuilder {
    enum Role { BODY, NECK, HEAD, SNOUT, JAW, LIMB, TAIL, BONE, CLAW, CREST, PLATE, FRILL, SAIL, WING }

    /** Where a box's faces are on the texture: (u, v) is the top-left of the standard cube layout. */
    record Box(Role role, int u, int v, int w, int h, int d) {}

    static final class Part {
        final String name;
        final Part parent;
        final PartPose pose;
        final List<float[]> cubes = new ArrayList<>();
        final List<Part> children = new ArrayList<>();

        Part(String name, Part parent, PartPose pose) {
            this.name = name;
            this.parent = parent;
            this.pose = pose;
            if (parent != null) parent.children.add(this);
        }

        String path() {
            return parent == null || parent.parent == null ? name : parent.path() + "/" + name;
        }
    }

    record Result(LayerDefinition layer, List<Box> boxes, int texWidth, int texHeight, List<String> paths) {}

    static final int TEX_WIDTH = 128;

    private final List<Box> boxes = new ArrayList<>();
    private final Part root = new Part("root", null, PartPose.ZERO);
    private final List<Part> parts = new ArrayList<>();
    private int cursorU, cursorV, rowHeight;

    private Part part(Part parent, String name, float x, float y, float z, float xRot) {
        Part part = new Part(name, parent, PartPose.offsetAndRotation(x, y, z, xRot, 0, 0));
        parts.add(part);
        return part;
    }

    private void box(Part part, Role role, float x, float y, float z, int w, int h, int d) {
        w = Math.max(1, w);
        h = Math.max(1, h);
        d = Math.max(1, d);
        int uvW = 2 * (w + d), uvH = h + d;
        if (cursorU + uvW > TEX_WIDTH) {
            cursorU = 0;
            cursorV += rowHeight;
            rowHeight = 0;
        }
        boxes.add(new Box(role, cursorU, cursorV, w, h, d));
        part.cubes.add(new float[]{x, y, z, w, h, d, cursorU, cursorV});
        cursorU += uvW;
        rowHeight = Math.max(rowHeight, uvH);
    }

    private static int r(float v) {
        return Math.max(1, Math.round(v));
    }

    static Result build(DinoSpec spec) {
        DinoModelBuilder b = new DinoModelBuilder();
        switch (spec.body()) {
            case THEROPOD, ORNITHOPOD -> b.biped(spec);
            case SAUROPOD -> b.sauropod(spec);
            case QUADRUPED -> b.quadruped(spec);
            case PTEROSAUR -> b.pterosaur(spec);
            case MARINE -> b.marine(spec);
        }
        return b.finish();
    }

    private Result finish() {
        MeshDefinition mesh = new MeshDefinition();
        for (Part child : root.children) add(mesh.getRoot(), child);
        int height = 16;
        while (height < cursorV + rowHeight) height *= 2;
        List<String> paths = new ArrayList<>();
        for (Part part : parts) paths.add(part.path());
        return new Result(LayerDefinition.create(mesh, TEX_WIDTH, height), boxes, TEX_WIDTH, height, paths);
    }

    private static void add(PartDefinition parent, Part part) {
        CubeListBuilder cubes = CubeListBuilder.create();
        for (float[] c : part.cubes) cubes.texOffs((int) c[6], (int) c[7]).addBox(c[0], c[1], c[2], c[3], c[4], c[5]);
        PartDefinition definition = parent.addOrReplaceChild(part.name, cubes, part.pose);
        for (Part child : part.children) add(definition, child);
    }

    // --- Two legs: theropods and ornithopods ---

    private void biped(DinoSpec spec) {
        Shape sh = spec.shape();
        boolean plantEater = spec.diet() == DinoSpec.Diet.HERBIVORE;
        int bodyLen = r(14 * sh.body()), bodyW = r(7 * sh.body()), bodyH = r(8 * sh.body());
        Part body = part(root, "body", 0, 8, 0, 0);
        box(body, Role.BODY, -bodyW / 2f, -bodyH / 2f, -bodyLen * 0.6f, bodyW, bodyH, bodyLen);
        if (spec.has(Feature.SAIL)) {
            int sail = r(bodyH * 1.1f);
            box(body, Role.SAIL, -0.5f, -bodyH / 2f - sail, -bodyLen * 0.5f, 1, sail, r(bodyLen * 0.8f));
        }

        // Neck and head
        int neckLen = r(6 * sh.neck()), neckW = Math.max(3, r(bodyW * 0.55f));
        float neckAngle = sh.neck() > 1.3f ? -0.85f : -0.55f;
        Part neck = part(body, "neck", 0, -bodyH / 2f + 2, -bodyLen * 0.6f + 1, neckAngle);
        box(neck, Role.NECK, -neckW / 2f, -neckW / 2f, -neckLen, neckW, neckW + 1, neckLen);
        Part head = part(neck, "head", 0, 0, -neckLen + 1, -neckAngle);
        head(head, spec, plantEater);

        // Tail
        tail(body, spec, 0, -bodyH / 2f + 2, bodyLen * 0.4f - 1, Math.max(2, bodyW - 2), Math.max(2, bodyH - 3), 9 * sh.tail(), -0.05f);

        // Legs (children of the root, so the body can bob without moving the feet)
        int thigh = Math.max(3, r(bodyW * 0.45f)), shin = Math.max(2, thigh - 1);
        for (int side = -1; side <= 1; side += 2) {
            String s = side < 0 ? "r" : "l";
            Part leg = part(root, "leg_" + s, side * (bodyW / 2f - thigh / 2f + 0.5f), 8, 1, -0.35f);
            box(leg, Role.LIMB, -thigh / 2f, -1, -thigh / 2f, thigh, 10, thigh);
            Part lower = part(leg, "shin", 0, 7.5f, 0, 0.7f);
            box(lower, Role.LIMB, -shin / 2f, 0, -shin / 2f, shin, 8, shin);
            Part foot = part(lower, "foot", 0, 7.5f, 0, -0.35f);
            box(foot, Role.LIMB, -shin / 2f - 0.5f, 0, -4, shin + 1, 2, 5);
            box(foot, Role.CLAW, -shin / 2f - 0.5f, 1, -5, shin + 1, 1, 1);
            if (spec.has(Feature.SICKLE_CLAW)) box(foot, Role.CLAW, -0.5f, -2, -2, 1, 2, 2);

            int armLen = r(5 * sh.arms()), armW = Math.max(1, r(2 * Math.min(1.5f, sh.arms())));
            Part arm = part(body, "arm_" + s, side * (bodyW / 2f), bodyH / 2f - 2, -bodyLen * 0.6f + 3, -0.6f);
            box(arm, Role.LIMB, -armW / 2f, 0, -armW / 2f, armW, armLen, armW);
            if (spec.has(Feature.LONG_CLAWS)) {
                box(arm, Role.CLAW, -0.5f, armLen, -0.5f, 1, 4, 1);
            } else {
                box(arm, Role.CLAW, -armW / 2f, armLen, -armW / 2f - 0.5f, armW, 1, 1);
            }
            if (spec.has(Feature.THUMB_SPIKE)) box(arm, Role.BONE, -0.5f, armLen - 2, -2, 1, 1, 2);
        }
    }

    /** A biped's head: skull, snout, jaw and head features. */
    private void head(Part head, DinoSpec spec, boolean plantEater) {
        float hs = spec.shape().head();
        int skW = r(5 * hs), skH = r(5 * hs), skL = r(6 * hs);
        float snoutLen = 5 * hs;
        if (spec.has(Feature.LONG_SNOUT)) snoutLen *= 1.8f;
        if (spec.has(Feature.BEAK)) snoutLen *= 0.8f;
        int snL = r(snoutLen), snW = spec.has(Feature.DUCKBILL) ? skW : Math.max(2, r(skW * 0.7f));
        int snH = spec.has(Feature.DUCKBILL) ? 2 : Math.max(2, r(skH * 0.55f));
        box(head, Role.HEAD, -skW / 2f, -skH, -skL, skW, skH, skL);
        box(head, Role.SNOUT, -snW / 2f, -skH + 1, -skL - snL, snW, snH, snL);
        Part jaw = part(head, "jaw", 0, -skH + 1 + snH, -1, 0.05f);
        box(jaw, Role.JAW, -snW / 2f + (plantEater ? 0.5f : 0), 0, -(snL + skL - 2), Math.max(1, snW - (plantEater ? 1 : 0)), Math.max(1, r(1.5f * hs)), snL + skL - 2);

        if (spec.has(Feature.CREST_DOUBLE)) {
            box(head, Role.CREST, -1.5f, -skH - 3, -skL - snL + 1, 1, 3, snL + skL - 2);
            box(head, Role.CREST, 0.5f, -skH - 3, -skL - snL + 1, 1, 3, snL + skL - 2);
        }
        if (spec.has(Feature.NOSE_HORN)) box(head, Role.BONE, -0.5f, -skH - 1, -skL - snL + 1, 1, 2, 2);
        if (spec.has(Feature.BROW_HORNS)) {
            box(head, Role.BONE, -skW / 2f, -skH - 2, -skL + 1, 1, 2, 2);
            box(head, Role.BONE, skW / 2f - 1, -skH - 2, -skL + 1, 1, 2, 2);
        }
        if (spec.has(Feature.CREST_SMALL)) box(head, Role.CREST, -1, -skH - 2, -skL + 1, 2, 2, 3);
        if (spec.has(Feature.CREST_HELMET)) box(head, Role.CREST, -1, -skH - 4, -skL, 2, 4, Math.max(3, skL - 1));
        if (spec.has(Feature.DOME)) box(head, Role.CREST, -skW / 2f, -skH - 2, -skL, skW, 2, skL);
        if (spec.has(Feature.CREST_TUBE)) {
            Part crest = part(head, "crest", 0, -skH, -2, -0.35f);
            box(crest, Role.CREST, -1, -1, 0, 2, 2, r(10 * hs));
        }
    }

    /** Tail of up to three tapering segments starting at (x, y, z) on {@code parent}, pointing back (+z). */
    private void tail(Part parent, DinoSpec spec, float x, float y, float z, int w, int h, float segLen, float angle) {
        Part t1 = part(parent, "tail", x, y, z, angle);
        int l1 = r(segLen);
        box(t1, Role.TAIL, -w / 2f, -h / 2f, 0, w, h, l1);
        int w2 = Math.max(1, w - 2), h2 = Math.max(1, h - 2), l2 = r(segLen);
        Part t2 = part(t1, "tail2", 0, 0, l1 - 1, -0.05f);
        box(t2, Role.TAIL, -w2 / 2f, -h2 / 2f, 0, w2, h2, l2);
        int w3 = Math.max(1, w2 - 2), h3 = Math.max(1, h2 - 1), l3 = r(segLen * 0.9f);
        Part t3 = part(t2, "tail3", 0, 0, l2 - 1, -0.05f);
        box(t3, Role.TAIL, -w3 / 2f, -h3 / 2f, 0, w3, h3, l3);

        if (spec.has(Feature.PLATES)) {
            for (int i = 0; i < 2; i++) box(t1, Role.PLATE, -0.5f, -h / 2f - 3 + i, 1 + i * (l1 / 2f), 1, 3 - i, 3);
        }
        if (spec.has(Feature.BACK_SPIKES)) {
            box(t1, Role.BONE, -0.5f, -h / 2f - 3, l1 / 2f, 1, 3, 1);
            box(t2, Role.BONE, -0.5f, -h2 / 2f - 3, l2 / 3f, 1, 3, 1);
        }
        if (spec.has(Feature.TAIL_SPIKES)) {
            for (int i = 0; i < 2; i++) {
                float at = l3 - 2 - i * 3;
                box(t3, Role.BONE, -w3 / 2f - 4, -1, at, 4, 1, 1);
                box(t3, Role.BONE, w3 / 2f, -1, at, 4, 1, 1);
            }
        }
        if (spec.has(Feature.TAIL_CLUB)) box(t3, Role.PLATE, -2.5f, -1.5f, l3 - 3, 5, 3, 4);
    }

    // --- Long-necked giants ---

    private void sauropod(DinoSpec spec) {
        Shape sh = spec.shape();
        int bodyLen = r(18 * sh.body()), bodyW = r(10 * sh.body()), bodyH = r(10 * sh.body());
        int backLeg = 14, frontLeg = r(14 * sh.frontLegs());
        float tilt = -(frontLeg - backLeg) / (bodyLen * 0.9f);
        Part body = part(root, "body", 0, 7 - (frontLeg - backLeg) / 2f, 0, tilt);
        box(body, Role.BODY, -bodyW / 2f, -bodyH / 2f, -bodyLen / 2f, bodyW, bodyH, bodyLen);
        legs(body, bodyLen, bodyW, frontLeg, backLeg);

        int segments = sh.neck() >= 1.5f ? 3 : 2;
        int segLen = r(20 * sh.neck() / segments);
        float firstAngle = sh.frontLegs() > 1 ? -1.0f : sh.neck() > 2 ? -0.75f : -0.6f;
        Part previous = body;
        float y = -bodyH / 2f + 3, z = -bodyLen / 2f + 1;
        for (int i = 0; i < segments; i++) {
            int w = Math.max(3, 5 - i);
            Part neck = part(previous, i == 0 ? "neck" : "neck" + (i + 1), 0, y, z, i == 0 ? firstAngle - tilt : -0.12f);
            box(neck, Role.NECK, -w / 2f, -w / 2f, -segLen, w, w, segLen);
            if (spec.has(Feature.NECK_SPINES)) {
                for (int k = 1; k < segLen - 1; k += 3) {
                    box(neck, Role.BONE, -1.5f, -w / 2f - 5, -k - 1, 1, 5, 1);
                    box(neck, Role.BONE, 0.5f, -w / 2f - 5, -k - 1, 1, 5, 1);
                }
            }
            previous = neck;
            y = 0;
            z = -segLen + 1;
        }
        float level = -(firstAngle - 0.12f * (segments - 1));
        Part head = part(previous, "head", 0, 0, z, level);
        box(head, Role.HEAD, -2, -2, -4, 4, 3, 5);
        box(head, Role.SNOUT, -1.5f, -1, -7, 3, 2, 3);
        Part jaw = part(head, "jaw", 0, 1, -3, 0.05f);
        box(jaw, Role.JAW, -1.5f, 0, -4, 3, 1, 4);

        tail(body, spec, 0, -bodyH / 2f + 3, bodyLen / 2f - 1, 6, 5, 10 * sh.tail(), 0.25f - tilt);
    }

    private void legs(Part body, int bodyLen, int bodyW, int frontLeg, int backLeg) {
        int w = Math.max(3, r(bodyW * 0.35f));
        for (int front = 0; front <= 1; front++) {
            int len = front == 1 ? frontLeg : backLeg;
            float z = front == 1 ? -bodyLen / 2f + w / 2f + 1 : bodyLen / 2f - w / 2f - 1;
            for (int side = -1; side <= 1; side += 2) {
                String name = "leg_" + (front == 1 ? "f" : "b") + (side < 0 ? "r" : "l");
                Part leg = part(root, name, side * (bodyW / 2f - w / 2f), 24 - len, z, 0);
                box(leg, Role.LIMB, -w / 2f, 0, -w / 2f, w, len, w);
                box(leg, Role.CLAW, -w / 2f, len - 1, -w / 2f - 0.5f, w, 1, 1);
            }
        }
    }

    // --- Flying reptiles ---

    private void pterosaur(DinoSpec spec) {
        Shape sh = spec.shape();
        Part body = part(root, "body", 0, 17, 0, 0);
        box(body, Role.BODY, -2.5f, -2.5f, -5, 5, 5, 9);
        for (int side = -1; side <= 1; side += 2) {
            Part leg = part(root, side < 0 ? "leg_r" : "leg_l", side * 1.5f, 19, 3, 0);
            box(leg, Role.LIMB, -0.5f, 0, -0.5f, 1, 5, 1);
            box(leg, Role.CLAW, -1, 4, -1.5f, 2, 1, 2);

            int inner = r(10 * sh.arms()), outer = r(11 * sh.arms());
            Part wing = part(body, side < 0 ? "wing_r" : "wing_l", side * 2.5f, -1.5f, -3, 0);
            box(wing, Role.LIMB, side < 0 ? -inner : 0, -0.5f, -0.5f, inner, 1, 1);
            box(wing, Role.WING, side < 0 ? -inner : 0, 0, 0.5f, inner, 1, 6);
            Part tip = part(wing, side < 0 ? "wing_tip_r" : "wing_tip_l", side * inner, 0, 0, 0);
            box(tip, Role.LIMB, side < 0 ? -outer : 0, -0.5f, -0.5f, outer, 1, 1);
            box(tip, Role.WING, side < 0 ? -outer : 0, 0, 0.5f, outer, 1, 4);
        }
        int neckLen = r(4 * sh.neck());
        Part neck = part(body, "neck", 0, -1.5f, -4.5f, -0.7f);
        box(neck, Role.NECK, -1.25f, -1.25f, -neckLen, 3, 3, neckLen);
        Part head = part(neck, "head", 0, 0, -neckLen + 0.5f, 0.7f);
        int beak = r(8 * sh.head());
        box(head, Role.HEAD, -1.5f, -2, -3, 3, 3, 3);
        box(head, Role.SNOUT, -0.75f, -1, -3 - beak, 2, 2, beak);
        Part jaw = part(head, "jaw", 0, 1, -2, 0.05f);
        box(jaw, Role.JAW, -0.5f, 0, -beak - 1, 1, 1, beak);
        if (spec.has(Feature.CREST_BACK)) box(head, Role.CREST, -0.5f, -4, -1, 1, 3, 7);
        Part tail = part(body, "tail", 0, -1, 4, 0.1f);
        box(tail, Role.TAIL, -0.5f, -0.5f, 0, 1, 1, r(3 * sh.tail()));
    }

    // --- Marine reptiles ---

    private void marine(DinoSpec spec) {
        Shape sh = spec.shape();
        int bodyLen = r(18 * sh.body()), bodyW = r(7 * sh.body()), bodyH = r(6 * sh.body());
        Part body = part(root, "body", 0, 18, 0, 0);
        box(body, Role.BODY, -bodyW / 2f, -bodyH / 2f, -bodyLen / 2f, bodyW, bodyH, bodyLen);
        if (spec.has(Feature.DORSAL_FIN)) box(body, Role.PLATE, -0.5f, -bodyH / 2f - 4, -2, 1, 4, 5);

        // Neck: long for plesiosaurs, barely there for the rest.
        Part previous = body;
        float z = -bodyLen / 2f + 1;
        int segments = sh.neck() > 1.3f ? 3 : 1;
        int segLen = sh.neck() > 1.3f ? r(14 * sh.neck() / segments) : r(3 * sh.neck());
        for (int i = 0; i < segments; i++) {
            int w = Math.max(2, r(bodyW * (sh.neck() > 1.3f ? 0.45f : 0.8f)) - i);
            Part neck = part(previous, i == 0 ? "neck" : "neck" + (i + 1), 0, 0, z, i == 0 ? -0.15f : -0.05f);
            box(neck, Role.NECK, -w / 2f, -w / 2f, -segLen, w, w, segLen);
            previous = neck;
            z = -segLen + 1;
        }
        float hs = sh.head();
        int skW = r(4 * hs), skH = r(3 * hs), skL = r(4 * hs);
        int snL = r(5 * hs * (spec.has(Feature.LONG_SNOUT) ? 1.8f : 1));
        Part head = part(previous, "head", 0, 0, z, 0.2f);
        box(head, Role.HEAD, -skW / 2f, -skH / 2f, -skL, skW, skH, skL);
        box(head, Role.SNOUT, -skW / 2f + 0.5f, -skH / 2f + 0.5f, -skL - snL, Math.max(1, skW - 1), Math.max(1, skH - 1), snL);
        Part jaw = part(head, "jaw", 0, skH / 2f - 0.5f, -1, 0.05f);
        box(jaw, Role.JAW, -skW / 2f + 0.5f, 0, -skL - snL + 1, Math.max(1, skW - 1), 1, skL + snL - 2);

        // Tail with an optional fluke.
        Part tail = part(body, "tail", 0, 0, bodyLen / 2f - 1, 0);
        int l1 = r(8 * sh.tail()), l2 = r(8 * sh.tail());
        box(tail, Role.TAIL, -bodyW / 2f + 1, -bodyH / 2f + 1, 0, Math.max(1, bodyW - 2), Math.max(1, bodyH - 2), l1);
        Part tail2 = part(tail, "tail2", 0, 0, l1 - 1, 0);
        box(tail2, Role.TAIL, -1, -1, 0, 2, 2, l2);
        if (spec.has(Feature.TAIL_FIN)) box(tail2, Role.PLATE, -0.5f, -5, l2 - 3, 1, 10, 3);

        // Flippers.
        int flipper = r(6 * sh.body());
        for (int front = 0; front <= 1; front++) {
            if (front == 0 && !spec.has(Feature.FOUR_FLIPPERS)) continue;
            float fz = front == 1 ? -bodyLen / 2f + 3 : bodyLen / 2f - 4;
            for (int side = -1; side <= 1; side += 2) {
                String name = "flipper_" + (front == 1 ? "f" : "b") + (side < 0 ? "r" : "l");
                Part flip = part(body, name, side * bodyW / 2f, bodyH / 2f - 1, fz, 0);
                box(flip, Role.LIMB, side < 0 ? -flipper : 0, -0.5f, -1.5f, flipper, 1, 3);
            }
        }
    }

    // --- Four legs: horned and armoured ---

    private void quadruped(DinoSpec spec) {
        Shape sh = spec.shape();
        int bodyLen = r(16 * sh.body()), bodyW = r(10 * sh.body()), bodyH = r(9 * sh.body());
        int backLeg = r(10 * sh.legs()), frontLeg = r(10 * sh.legs() * sh.frontLegs());
        float tilt = (backLeg - frontLeg) / (float) bodyLen;
        Part body = part(root, "body", 0, 24 - backLeg - bodyH / 2f + 2, 0, tilt);
        box(body, Role.BODY, -bodyW / 2f, -bodyH / 2f, -bodyLen / 2f, bodyW, bodyH, bodyLen);
        legs(body, bodyLen, bodyW, frontLeg, backLeg);

        if (spec.has(Feature.PLATES)) {
            int plates = Math.max(3, bodyLen / 4);
            for (int i = 0; i < plates; i++) {
                float t = i / (float) (plates - 1);
                int ph = r(3 + 4 * (float) Math.sin(Math.PI * (0.2 + 0.7 * t)));
                if (spec.has(Feature.BACK_SPIKES) && t > 0.55f) {
                    box(body, Role.BONE, -0.5f, -bodyH / 2f - ph, -bodyLen / 2f + 1 + t * (bodyLen - 4), 1, ph, 1);
                } else {
                    box(body, Role.PLATE, -0.5f, -bodyH / 2f - ph, -bodyLen / 2f + 1 + t * (bodyLen - 4), 1, ph, 3);
                }
            }
        }
        if (spec.has(Feature.SHOULDER_SPIKES)) {
            box(body, Role.BONE, -bodyW / 2f - 4, -1, -bodyLen / 2f + 3, 4, 1, 1);
            box(body, Role.BONE, bodyW / 2f, -1, -bodyLen / 2f + 3, 4, 1, 1);
        }
        if (spec.has(Feature.ARMOR)) {
            for (int x = -bodyW / 2 + 1; x < bodyW / 2 - 1; x += 3) {
                for (int z = -bodyLen / 2 + 1; z < bodyLen / 2 - 1; z += 3) box(body, Role.PLATE, x, -bodyH / 2f - 1, z, 2, 1, 2);
            }
            for (int z = -bodyLen / 2 + 2; z < bodyLen / 2 - 2; z += 4) {
                box(body, Role.BONE, -bodyW / 2f - 1, 0, z, 1, 1, 2);
                box(body, Role.BONE, bodyW / 2f, 0, z, 1, 1, 2);
            }
        }

        int nw = Math.max(3, r(bodyW * 0.5f));
        Part neck = part(body, "neck", 0, -1, -bodyLen / 2f + 1, 0.1f - tilt);
        box(neck, Role.NECK, -nw / 2f, -nw / 2f, -4, nw, nw, 4);
        float hs = sh.head();
        int hw = r(6 * hs), hh = r(6 * hs), hl = r(7 * hs);
        Part head = part(neck, "head", 0, 0, -3.5f, 0.05f);
        box(head, Role.HEAD, -hw / 2f, -hh / 2f, -hl, hw, hh, hl);
        box(head, Role.SNOUT, -hw / 2f + 1, -hh / 2f + 1, -hl - 3, Math.max(1, hw - 2), Math.max(1, hh - 2), 3);
        Part jaw = part(head, "jaw", 0, hh / 2f - 1, -1, 0.05f);
        box(jaw, Role.JAW, -hw / 2f + 1, 0, -(hl + 2), Math.max(1, hw - 2), 1, hl + 1);

        if (spec.has(Feature.FRILL) || spec.has(Feature.SPIKE_FRILL)) {
            Part frill = part(head, "frill", 0, -hh / 2f + 1, -1, -0.5f);
            int fw = hw + 6, fh = hh + 4;
            box(frill, Role.FRILL, -fw / 2f, -fh, 0, fw, fh, 1);
            if (spec.has(Feature.SPIKE_FRILL)) {
                for (int i = 0; i < 4; i++) box(frill, Role.BONE, -fw / 2f + 1 + i * (fw - 2) / 3f - 0.5f, -fh - 4, 0, 1, 4, 1);
            }
        }
        if (spec.has(Feature.HORNS_THREE)) {
            for (int side = -1; side <= 1; side += 2) {
                Part horn = part(head, side < 0 ? "horn_r" : "horn_l", side * (hw / 2f - 1.5f), -hh / 2f, -hl + 2, 1.0f);
                box(horn, Role.BONE, -0.5f, -7, -0.5f, 1, 7, 1);
            }
            Part nose = part(head, "horn_nose", 0, -hh / 2f + 1, -hl - 2, 0.6f);
            box(nose, Role.BONE, -0.5f, -2, -0.5f, 1, 2, 1);
        }
        if (spec.has(Feature.NOSE_HORN)) {
            Part nose = part(head, "horn_nose", 0, -hh / 2f + 1, -hl - 2, 0.5f);
            box(nose, Role.BONE, -0.5f, -5, -0.5f, 1, 5, 1);
        }

        tail(body, spec, 0, -bodyH / 2f + 2, bodyLen / 2f - 1, Math.max(2, bodyW - 3), Math.max(2, bodyH - 3), 8 * sh.tail(), 0.15f - tilt);
    }
}
