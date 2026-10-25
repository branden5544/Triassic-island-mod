package com.triassicisland.client.dino;

import com.mojang.blaze3d.platform.NativeImage;
import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinoSpec.Pattern;
import com.triassicisland.client.dino.DinoModelBuilder.Box;
import com.triassicisland.client.dino.DinoModelBuilder.Role;

import java.util.Random;

/**
 * Paints a species' skin onto the exact texture layout its model uses: darker back, light belly, stripes or
 * spots, eyes on the head, teeth for meat eaters, ivory horns and dark claws.
 */
final class DinoSkin {
    private static final int IVORY = 0xE6DDC6, CLAW = 0x2A2A2A, EYE = 0x141210, EYE_SHINE = 0xD8C060, TOOTH = 0xF2F0E2;

    private final NativeImage image;
    private final DinoSpec spec;
    private final Random random;

    private DinoSkin(NativeImage image, DinoSpec spec) {
        this.image = image;
        this.spec = spec;
        this.random = new Random(spec.species().ordinal() * 7919L);
    }

    static NativeImage paint(DinoModelBuilder.Result model, DinoSpec spec) {
        NativeImage image = new NativeImage(model.texWidth(), model.texHeight(), true);
        DinoSkin skin = new DinoSkin(image, spec);
        for (Box box : model.boxes()) skin.paint(box);
        return image;
    }

    private void paint(Box b) {
        int u = b.u(), v = b.v(), w = b.w(), h = b.h(), d = b.d();
        switch (b.role()) {
            case BONE -> fill(u, v, 2 * (w + d), h + d, IVORY, 10);
            case WING -> {
                // Wing membrane: between back and belly colour, with darker veins.
                int membrane = mix(spec.baseColor(), spec.bellyColor());
                fill(u, v, 2 * (w + d), h + d, membrane, 10);
                for (int x = u + 2; x < u + 2 * (w + d); x += 4) line(x, v, h + d, shade(membrane, 0.8f));
            }
            case CLAW -> fill(u, v, 2 * (w + d), h + d, CLAW, 6);
            case CREST, SAIL, FRILL, PLATE -> {
                fill(u, v, 2 * (w + d), h + d, spec.patternColor(), 14);
                // Veins / edge.
                for (int x = u; x < u + 2 * (w + d); x += 3) line(x, v + d, h, shade(spec.patternColor(), 0.75f));
                rect(u + d, v, w, d, shade(spec.patternColor(), 1.2f));
            }
            default -> skin(b);
        }
    }

    /** Body-like parts: base colour, lighter underside, pattern on the back, and face details. */
    private void skin(Box b) {
        int u = b.u(), v = b.v(), w = b.w(), h = b.h(), d = b.d();
        int base = b.role() == Role.LIMB ? shade(spec.baseColor(), 0.85f) : spec.baseColor();
        fill(u, v, 2 * (w + d), h + d, base, 12);
        // Underside, and the lower part of every side.
        rect(u + d + w, v, w, d, spec.bellyColor());
        int bellyRows = b.role() == Role.LIMB ? 0 : Math.max(1, Math.round(h * 0.35f));
        if (b.role() == Role.JAW) bellyRows = h;
        rect(u, v + d + h - bellyRows, 2 * (w + d), bellyRows, spec.bellyColor());

        boolean patterned = b.role() == Role.BODY || b.role() == Role.TAIL || b.role() == Role.NECK || b.role() == Role.HEAD;
        if (patterned && spec.pattern() == Pattern.STRIPES) {
            int stripe = spec.patternColor();
            for (int z = 1; z < d; z += 4) {
                rect(u + d, v + z, w, 1, stripe);                         // across the back
                rect(u + d - 1 - z, v + d, 1, Math.max(1, h / 2), stripe); // right side
                rect(u + d + w + z, v + d, 1, Math.max(1, h / 2), stripe); // left side
            }
        } else if (patterned && spec.pattern() == Pattern.SPOTS) {
            int spots = Math.max(2, (w * d + d * h) / 10);
            for (int i = 0; i < spots; i++) {
                dot(u + d + random.nextInt(w), v + random.nextInt(d), spec.patternColor());
                dot(u + random.nextInt(d), v + d + random.nextInt(Math.max(1, h / 2 + 1)), spec.patternColor());
                dot(u + d + w + random.nextInt(d), v + d + random.nextInt(Math.max(1, h / 2 + 1)), spec.patternColor());
            }
        }

        if (b.role() == Role.HEAD) {
            // Eyes near the front top of each side. The right side's front edge is at u + d, the left side's at u + d + w.
            int eyeY = v + d + Math.min(1, h - 1);
            set(u + d - 2, eyeY, EYE);
            set(u + d + w + 1, eyeY, EYE);
            if (h >= 4) {
                set(u + d - 2, eyeY + 1, EYE_SHINE);
                set(u + d + w + 1, eyeY + 1, EYE_SHINE);
            }
        }
        if (spec.isCarnivore() && (b.role() == Role.SNOUT || b.role() == Role.JAW)) {
            int row = b.role() == Role.SNOUT ? v + d + h - 1 : v + d;
            for (int x = u; x < u + 2 * (w + d); x += 2) set(x, row, TOOTH);
        }
        if (b.role() == Role.SNOUT) {
            set(u + d, v + d, shade(base, 0.5f));
            set(u + d + w - 1, v + d, shade(base, 0.5f));
        }
    }

    // --- Drawing helpers (colours are 0xRRGGBB) ---

    private void fill(int x, int y, int w, int h, int color, int noise) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) set(px, py, jitter(color, noise));
        }
    }

    private void rect(int x, int y, int w, int h, int color) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) set(px, py, jitter(color, 8));
        }
    }

    private void line(int x, int y, int h, int color) {
        for (int py = y; py < y + h; py++) set(x, py, color);
    }

    private void dot(int x, int y, int color) {
        set(x, y, color);
        if (random.nextBoolean()) set(x + 1, y, color);
    }

    private void set(int x, int y, int rgb) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
        int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
        image.setPixelRGBA(x, y, 0xFF000000 | b << 16 | g << 8 | r);
    }

    private int jitter(int rgb, int amount) {
        int delta = random.nextInt(amount * 2 + 1) - amount;
        return clamp((rgb >> 16 & 0xFF) + delta) << 16 | clamp((rgb >> 8 & 0xFF) + delta) << 8 | clamp((rgb & 0xFF) + delta);
    }

    private static int shade(int rgb, float factor) {
        return clamp(Math.round((rgb >> 16 & 0xFF) * factor)) << 16 | clamp(Math.round((rgb >> 8 & 0xFF) * factor)) << 8
                | clamp(Math.round((rgb & 0xFF) * factor));
    }

    private static int mix(int a, int b) {
        return ((a >> 16 & 0xFF) + (b >> 16 & 0xFF)) / 2 << 16 | ((a >> 8 & 0xFF) + (b >> 8 & 0xFF)) / 2 << 8 | ((a & 0xFF) + (b & 0xFF)) / 2;
    }

    private static int clamp(int c) {
        return Math.max(0, Math.min(255, c));
    }
}
