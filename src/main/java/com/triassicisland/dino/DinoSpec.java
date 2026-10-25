package com.triassicisland.dino;

import com.triassicisland.dna.Species;

import java.util.EnumSet;
import java.util.Set;

/**
 * Everything that makes one species look and behave the way it does. Models are built from the body plan,
 * proportions and features; skins are painted from the colours.
 *
 * @param scale adult size multiplier: roughly hip height in blocks for bipeds and quadrupeds
 */
public record DinoSpec(Species species, Body body, float scale, float health, float damage, float speed, float armor,
                       Diet diet, Temper temper, Size size, Set<Feature> features, Shape shape,
                       int baseColor, int bellyColor, int patternColor, Pattern pattern) {

    public enum Body { THEROPOD, ORNITHOPOD, SAUROPOD, QUADRUPED, PTEROSAUR, MARINE }

    public enum Diet { CARNIVORE, PISCIVORE, HERBIVORE }

    public enum Temper {
        /** Attacks players and animals on sight. */
        AGGRESSIVE,
        /** Hunts animals when hungry, players only when starving. */
        HUNTER,
        /** Fights back when attacked. */
        DEFENSIVE,
        /** Runs away. */
        DOCILE
    }

    /** Small ones can't break fences, medium ones break unpowered standard fences, large ones break any unpowered fence. */
    public enum Size { SMALL, MEDIUM, LARGE }

    public enum Pattern { NONE, STRIPES, SPOTS }

    public enum Feature {
        CREST_DOUBLE, SAIL, NOSE_HORN, BROW_HORNS, BEAK, LONG_SNOUT, SICKLE_CLAW, LONG_CLAWS, CREST_SMALL,
        CREST_TUBE, DOME, DUCKBILL, THUMB_SPIKE, FRILL, SPIKE_FRILL, HORNS_THREE, PLATES, BACK_SPIKES,
        TAIL_SPIKES, SHOULDER_SPIKES, ARMOR, TAIL_CLUB, CREST_HELMET, NECK_SPINES,
        /** Pteranodon's backward crest. */
        CREST_BACK,
        /** Marine reptiles. */
        TAIL_FIN, DORSAL_FIN, FOUR_FLIPPERS,
        /** Tests fences for weak spots even when not hungry, and can break standard fences regardless of size. */
        CLEVER
    }

    /** Proportions relative to the body plan's default (1 = normal). */
    public record Shape(float head, float neck, float tail, float arms, float legs, float body, float frontLegs) {
        public static final Shape NORMAL = new Shape(1, 1, 1, 1, 1, 1, 1);

        public Shape head(float v) { return new Shape(v, neck, tail, arms, legs, body, frontLegs); }
        public Shape neck(float v) { return new Shape(head, v, tail, arms, legs, body, frontLegs); }
        public Shape tail(float v) { return new Shape(head, neck, v, arms, legs, body, frontLegs); }
        public Shape arms(float v) { return new Shape(head, neck, tail, v, legs, body, frontLegs); }
        public Shape legs(float v) { return new Shape(head, neck, tail, arms, v, body, frontLegs); }
        public Shape body(float v) { return new Shape(head, neck, tail, arms, legs, v, frontLegs); }
        public Shape frontLegs(float v) { return new Shape(head, neck, tail, arms, legs, body, v); }
    }

    public boolean has(Feature feature) {
        return features.contains(feature);
    }

    public boolean flies() {
        return body == Body.PTEROSAUR;
    }

    public boolean swims() {
        return body == Body.MARINE;
    }

    public boolean isCarnivore() {
        return diet != Diet.HERBIVORE;
    }

    /** Hitbox of an adult, in blocks. */
    public float width() {
        float w = switch (body) {
            case THEROPOD, ORNITHOPOD -> 0.65f;
            case SAUROPOD -> 0.85f;
            case QUADRUPED -> 0.85f;
            case PTEROSAUR -> 0.9f;
            case MARINE -> 0.9f;
        };
        return Math.min(2.8f, w * scale * shape.body());
    }

    public float height() {
        return switch (body) {
            case THEROPOD, ORNITHOPOD -> 1.55f * scale;
            case SAUROPOD -> 2.2f * scale * Math.max(1, shape.neck() * 0.8f);
            case QUADRUPED -> 1.05f * scale;
            case PTEROSAUR -> 0.8f * scale;
            case MARINE -> 0.7f * scale;
        };
    }

    static Set<Feature> features(Feature... features) {
        return features.length == 0 ? EnumSet.noneOf(Feature.class) : EnumSet.of(features[0], features);
    }
}
