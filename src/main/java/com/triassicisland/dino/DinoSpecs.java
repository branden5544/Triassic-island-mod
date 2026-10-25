package com.triassicisland.dino;

import com.triassicisland.dna.Species;
import com.triassicisland.dino.DinoSpec.Body;
import com.triassicisland.dino.DinoSpec.Diet;
import com.triassicisland.dino.DinoSpec.Pattern;
import com.triassicisland.dino.DinoSpec.Shape;
import com.triassicisland.dino.DinoSpec.Size;
import com.triassicisland.dino.DinoSpec.Temper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static com.triassicisland.dino.DinoSpec.Feature.*;
import static com.triassicisland.dino.DinoSpec.features;

/** Every species that can hatch: land dinosaurs, flying reptiles and marine reptiles. */
public final class DinoSpecs {
    private static final Map<Species, DinoSpec> SPECS = new EnumMap<>(Species.class);
    private static final Shape N = Shape.NORMAL;

    static {
        // --- Theropods ---
        add(Species.TYRANNOSAURUS, Body.THEROPOD, 2.6f, 220, 24, 0.28f, 4, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(), N.head(1.5f).arms(0.45f).neck(0.8f).body(1.2f), 0x6B5A3E, 0xB8A27A, 0x4A3B26, Pattern.STRIPES);
        add(Species.VELOCIRAPTOR, Body.THEROPOD, 0.9f, 30, 7, 0.38f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.SMALL,
                features(SICKLE_CLAW, CLEVER), N.head(0.9f).tail(1.25f).arms(1.2f), 0xA88B5C, 0xD9C9A3, 0x6B4F2E, Pattern.STRIPES);
        add(Species.DILOPHOSAURUS, Body.THEROPOD, 1.2f, 40, 8, 0.33f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.MEDIUM,
                features(CREST_DOUBLE), N.neck(1.2f), 0x7A8C4A, 0xD8D0A0, 0xC9A23A, Pattern.SPOTS);
        add(Species.SPINOSAURUS, Body.THEROPOD, 2.6f, 200, 20, 0.27f, 4, Diet.PISCIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(SAIL, LONG_SNOUT), N.head(1.3f).arms(1.2f).body(1.2f), 0x6E6A5A, 0xBDB59A, 0xA0522D, Pattern.STRIPES);
        add(Species.ALLOSAURUS, Body.THEROPOD, 2.0f, 150, 18, 0.30f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(BROW_HORNS), N.head(1.2f), 0x8A6A4A, 0xC8B090, 0x5A3A2A, Pattern.STRIPES);
        add(Species.CARNOTAURUS, Body.THEROPOD, 1.9f, 130, 16, 0.34f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(BROW_HORNS), N.arms(0.3f).head(1.1f), 0x8E3B2E, 0xC9A27A, 0x5A2A20, Pattern.SPOTS);
        add(Species.BARYONYX, Body.THEROPOD, 1.8f, 120, 14, 0.30f, 2, Diet.PISCIVORE, Temper.HUNTER, Size.LARGE,
                features(LONG_SNOUT), N.arms(1.3f), 0x5E7A5A, 0xC8C8A0, 0x3E5A3A, Pattern.STRIPES);
        add(Species.COMPSOGNATHUS, Body.THEROPOD, 0.35f, 8, 2, 0.36f, 0, Diet.CARNIVORE, Temper.HUNTER, Size.SMALL,
                features(), N.neck(1.2f).tail(1.2f), 0x7FA35A, 0xE0D8A8, 0x5A7A3A, Pattern.STRIPES);
        add(Species.GIGANOTOSAURUS, Body.THEROPOD, 2.8f, 240, 26, 0.28f, 4, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(), N.head(1.5f).arms(0.55f).body(1.2f), 0x7A6A5A, 0xC0B098, 0x4A3A30, Pattern.STRIPES);
        add(Species.CERATOSAURUS, Body.THEROPOD, 1.7f, 110, 14, 0.31f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(NOSE_HORN, BROW_HORNS), N.head(1.15f), 0x5A4A3A, 0xB09070, 0xC06030, Pattern.STRIPES);
        add(Species.COELOPHYSIS, Body.THEROPOD, 0.7f, 20, 4, 0.38f, 0, Diet.CARNIVORE, Temper.HUNTER, Size.SMALL,
                features(), N.neck(1.4f).head(0.8f).tail(1.2f), 0xB08850, 0xE8D8B0, 0x805830, Pattern.STRIPES);
        add(Species.HERRERASAURUS, Body.THEROPOD, 1.1f, 45, 9, 0.35f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.MEDIUM,
                features(), N, 0x8A5A3A, 0xD0B890, 0x5A3A2A, Pattern.STRIPES);
        add(Species.UTAHRAPTOR, Body.THEROPOD, 1.4f, 70, 12, 0.36f, 1, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.MEDIUM,
                features(SICKLE_CLAW, CLEVER), N.arms(1.3f).tail(1.2f), 0x6A5A4A, 0xC0B090, 0x3A2A20, Pattern.STRIPES);
        add(Species.GALLIMIMUS, Body.THEROPOD, 1.2f, 35, 3, 0.42f, 0, Diet.HERBIVORE, Temper.DOCILE, Size.MEDIUM,
                features(BEAK), N.neck(1.6f).arms(1.1f).head(0.8f), 0xA09070, 0xE0D8C0, 0x706040, Pattern.NONE);
        add(Species.THERIZINOSAURUS, Body.THEROPOD, 2.0f, 140, 16, 0.25f, 2, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(BEAK, LONG_CLAWS), N.arms(2.0f).neck(1.5f).head(0.8f).body(1.15f), 0x6A6A50, 0xB0A888, 0x4A4A38, Pattern.NONE);
        add(Species.OVIRAPTOR, Body.THEROPOD, 0.7f, 18, 3, 0.36f, 0, Diet.HERBIVORE, Temper.DOCILE, Size.SMALL,
                features(BEAK, CREST_SMALL), N.arms(1.2f), 0x5A6A8A, 0xD0D0D0, 0x3A4A6A, Pattern.STRIPES);

        // --- Ornithopods (two-legged plant eaters) ---
        add(Species.PARASAUROLOPHUS, Body.ORNITHOPOD, 2.0f, 120, 6, 0.30f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(CREST_TUBE, DUCKBILL), N.arms(1.4f).body(1.1f), 0x8A7A4A, 0xD8C8A0, 0xA05A30, Pattern.STRIPES);
        add(Species.PACHYCEPHALOSAURUS, Body.ORNITHOPOD, 1.2f, 60, 10, 0.32f, 2, Diet.HERBIVORE, Temper.DEFENSIVE, Size.MEDIUM,
                features(DOME, BEAK), N, 0x8A6A4A, 0xD0B890, 0x5A4030, Pattern.SPOTS);
        add(Species.EDMONTOSAURUS, Body.ORNITHOPOD, 2.2f, 140, 6, 0.29f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(DUCKBILL), N.arms(1.4f).body(1.15f), 0x6A7A5A, 0xC8C8A8, 0x4A5A3A, Pattern.STRIPES);
        add(Species.IGUANODON, Body.ORNITHOPOD, 2.0f, 130, 10, 0.28f, 2, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(THUMB_SPIKE, BEAK), N.arms(1.5f).body(1.15f), 0x5A7A4A, 0xC0C0A0, 0x3A5A2A, Pattern.NONE);
        add(Species.PLATEOSAURUS, Body.ORNITHOPOD, 1.6f, 80, 6, 0.28f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.MEDIUM,
                features(), N.neck(1.8f).head(0.7f).arms(1.3f), 0x9A7A5A, 0xD8C8A8, 0x6A4A2A, Pattern.SPOTS);

        // --- Sauropods ---
        add(Species.BRACHIOSAURUS, Body.SAUROPOD, 3.4f, 350, 18, 0.22f, 6, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(), N.neck(1.6f).frontLegs(1.3f).tail(0.8f), 0x6A7A7A, 0xB0B8B0, 0x4A5A5A, Pattern.NONE);
        add(Species.APATOSAURUS, Body.SAUROPOD, 3.0f, 320, 16, 0.22f, 6, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(), N.neck(1.2f).tail(1.4f).body(1.15f), 0x7A6A5A, 0xC0B0A0, 0x5A4A3A, Pattern.NONE);
        add(Species.DIPLODOCUS, Body.SAUROPOD, 3.0f, 280, 14, 0.24f, 5, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(), N.neck(1.5f).tail(1.8f), 0x8A8A6A, 0xD0D0B8, 0x5A5A4A, Pattern.STRIPES);
        add(Species.MAMENCHISAURUS, Body.SAUROPOD, 3.0f, 300, 14, 0.22f, 5, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(), N.neck(2.2f), 0x7A7A5A, 0xC8C0A0, 0x55553A, Pattern.SPOTS);

        // --- Armoured and horned (four-legged) ---
        add(Species.TRICERATOPS, Body.QUADRUPED, 2.0f, 200, 18, 0.26f, 8, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(FRILL, HORNS_THREE, BEAK), N.head(1.3f), 0x6A6A5A, 0xB0A890, 0xA0603A, Pattern.NONE);
        add(Species.STEGOSAURUS, Body.QUADRUPED, 2.0f, 180, 16, 0.22f, 6, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(PLATES, TAIL_SPIKES, BEAK), N.frontLegs(0.7f).head(0.6f).tail(1.2f), 0x6A7A4A, 0xC0B890, 0xA0502A, Pattern.SPOTS);
        add(Species.ANKYLOSAURUS, Body.QUADRUPED, 1.7f, 220, 18, 0.20f, 14, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(ARMOR, TAIL_CLUB, BEAK), N.legs(0.75f).frontLegs(0.75f).body(1.25f).head(0.9f), 0x7A6A4A, 0xB0A080, 0x5A4A30, Pattern.SPOTS);
        add(Species.STYRACOSAURUS, Body.QUADRUPED, 1.8f, 170, 16, 0.27f, 7, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(SPIKE_FRILL, NOSE_HORN, BEAK), N.head(1.2f), 0x8A6A4A, 0xC8B090, 0xD08040, Pattern.NONE);
        add(Species.KENTROSAURUS, Body.QUADRUPED, 1.3f, 100, 12, 0.24f, 5, Diet.HERBIVORE, Temper.DEFENSIVE, Size.MEDIUM,
                features(PLATES, BACK_SPIKES, TAIL_SPIKES, SHOULDER_SPIKES, BEAK), N.frontLegs(0.75f).head(0.6f), 0x8A7A4A, 0xD0C098, 0x5A4A2A, Pattern.STRIPES);

        // --- Flying reptiles (fly; fold their wings on the ground) ---
        add(Species.PTERANODON, Body.PTEROSAUR, 1.3f, 40, 6, 0.25f, 0, Diet.PISCIVORE, Temper.HUNTER, Size.MEDIUM,
                features(CREST_BACK), N.head(1.1f), 0x8A7A6A, 0xD8D0C0, 0xA04030, Pattern.NONE);
        add(Species.DIMORPHODON, Body.PTEROSAUR, 0.6f, 15, 3, 0.28f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.SMALL,
                features(), N.head(1.4f).arms(0.7f).tail(2f), 0x5A6A4A, 0xC8C8A0, 0xC09030, Pattern.SPOTS);
        add(Species.QUETZALCOATLUS, Body.PTEROSAUR, 2.4f, 120, 14, 0.25f, 2, Diet.CARNIVORE, Temper.HUNTER, Size.LARGE,
                features(), N.neck(2f).head(1.3f), 0x9A9A90, 0xE0E0D8, 0xC04040, Pattern.NONE);

        // --- Marine reptiles (swim; must hatch in water) ---
        add(Species.MOSASAURUS, Body.MARINE, 3.0f, 300, 26, 1.1f, 6, Diet.PISCIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(TAIL_FIN, FOUR_FLIPPERS), N.head(1.4f).body(1.2f).tail(1.3f), 0x4A5A6A, 0xC8D0D0, 0x2A3A4A, Pattern.STRIPES);
        add(Species.PLESIOSAURUS, Body.MARINE, 1.8f, 120, 10, 1.0f, 2, Diet.PISCIVORE, Temper.HUNTER, Size.LARGE,
                features(FOUR_FLIPPERS), N.neck(2.2f).head(0.7f).tail(0.7f), 0x5A6A5A, 0xD0D0C0, 0x3A4A3A, Pattern.SPOTS);
        add(Species.ICHTHYOSAURUS, Body.MARINE, 1.4f, 60, 6, 1.3f, 0, Diet.PISCIVORE, Temper.DOCILE, Size.MEDIUM,
                features(TAIL_FIN, DORSAL_FIN, LONG_SNOUT), N.head(0.9f), 0x5A7090, 0xD8E0E8, 0x3A5070, Pattern.NONE);
        add(Species.LIOPLEURODON, Body.MARINE, 2.6f, 250, 22, 1.0f, 5, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(FOUR_FLIPPERS), N.head(1.6f).neck(0.8f).tail(0.8f), 0x5A4A4A, 0xC0B0A8, 0x3A2A2A, Pattern.SPOTS);

        // --- More land species ---
        add(Species.DEINONYCHUS, Body.THEROPOD, 1.1f, 45, 9, 0.37f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.MEDIUM,
                features(SICKLE_CLAW, CLEVER), N.arms(1.2f).tail(1.25f), 0x7A6050, 0xCFC0A0, 0x4A3020, Pattern.STRIPES);
        add(Species.TROODON, Body.THEROPOD, 0.6f, 16, 4, 0.38f, 0, Diet.CARNIVORE, Temper.HUNTER, Size.SMALL,
                features(SICKLE_CLAW, CLEVER), N.head(1.1f).tail(1.2f), 0x907858, 0xE0D4B8, 0x605038, Pattern.SPOTS);
        add(Species.SUCHOMIMUS, Body.THEROPOD, 2.1f, 150, 16, 0.29f, 3, Diet.PISCIVORE, Temper.HUNTER, Size.LARGE,
                features(LONG_SNOUT, CREST_SMALL), N.arms(1.3f).body(1.1f), 0x7A7050, 0xD0C8A8, 0x5A4A30, Pattern.STRIPES);
        add(Species.MAJUNGASAURUS, Body.THEROPOD, 1.7f, 110, 15, 0.31f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(CREST_SMALL), N.arms(0.35f).head(1.2f), 0x8A4A3A, 0xC8A888, 0x5A2A20, Pattern.STRIPES);
        add(Species.CONCAVENATOR, Body.THEROPOD, 1.5f, 90, 12, 0.32f, 1, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.MEDIUM,
                features(SAIL), N.body(0.8f), 0x6A5A40, 0xC8B898, 0xB06030, Pattern.STRIPES);
        add(Species.METRIACANTHOSAURUS, Body.THEROPOD, 1.9f, 140, 17, 0.30f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(BROW_HORNS), N.head(1.2f), 0x7A6A58, 0xC0B098, 0x9A5030, Pattern.STRIPES);
        add(Species.MEGALOSAURUS, Body.THEROPOD, 1.9f, 140, 18, 0.29f, 2, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.LARGE,
                features(), N.head(1.3f).body(1.1f), 0x6A5848, 0xB8A888, 0x4A3828, Pattern.SPOTS);
        add(Species.PYRORAPTOR, Body.THEROPOD, 0.8f, 25, 6, 0.38f, 0, Diet.CARNIVORE, Temper.AGGRESSIVE, Size.SMALL,
                features(SICKLE_CLAW, CLEVER), N.arms(1.3f).tail(1.3f), 0xB04A2A, 0xE8C8A0, 0x602010, Pattern.STRIPES);
        add(Species.PROCERATOSAURUS, Body.THEROPOD, 0.8f, 22, 5, 0.37f, 0, Diet.CARNIVORE, Temper.HUNTER, Size.SMALL,
                features(NOSE_HORN, CREST_SMALL), N, 0x8A7050, 0xD8C8A8, 0x5A4030, Pattern.STRIPES);
        add(Species.CORYTHOSAURUS, Body.ORNITHOPOD, 2.0f, 120, 6, 0.29f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(CREST_HELMET, DUCKBILL), N.arms(1.4f).body(1.1f), 0x6A7A4A, 0xD0D0A8, 0xB07040, Pattern.STRIPES);
        add(Species.LAMBEOSAURUS, Body.ORNITHOPOD, 2.1f, 125, 6, 0.29f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(CREST_SMALL, DUCKBILL), N.arms(1.4f).body(1.1f), 0x7A6A4A, 0xD8C8A0, 0x9A4A30, Pattern.STRIPES);
        add(Species.MAIASAURA, Body.ORNITHOPOD, 2.0f, 120, 6, 0.28f, 1, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(DUCKBILL), N.arms(1.4f).body(1.15f), 0x7A7058, 0xD0C8B0, 0x5A5038, Pattern.SPOTS);
        add(Species.OURANOSAURUS, Body.ORNITHOPOD, 1.9f, 110, 8, 0.29f, 1, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(SAIL, DUCKBILL, THUMB_SPIKE), N.arms(1.4f), 0x8A6A40, 0xD8C8A0, 0xC07840, Pattern.NONE);
        add(Species.HYPSILOPHODON, Body.ORNITHOPOD, 0.6f, 14, 2, 0.40f, 0, Diet.HERBIVORE, Temper.DOCILE, Size.SMALL,
                features(BEAK), N.tail(1.2f), 0x6A8A50, 0xD8E0B8, 0x4A6A30, Pattern.STRIPES);
        add(Species.STYGIMOLOCH, Body.ORNITHOPOD, 1.0f, 50, 9, 0.33f, 2, Diet.HERBIVORE, Temper.DEFENSIVE, Size.MEDIUM,
                features(DOME, BROW_HORNS, BEAK), N, 0x7A5A4A, 0xC8B098, 0x4A3020, Pattern.SPOTS);
        add(Species.ARGENTINOSAURUS, Body.SAUROPOD, 4.0f, 500, 24, 0.20f, 8, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(), N.neck(1.5f).tail(1.3f).body(1.1f), 0x7A7066, 0xC0B8B0, 0x5A5048, Pattern.NONE);
        add(Species.CAMARASAURUS, Body.SAUROPOD, 2.8f, 280, 15, 0.22f, 5, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(), N.neck(1.0f).head(1.2f), 0x8A7A60, 0xD0C8B0, 0x6A5A40, Pattern.SPOTS);
        add(Species.AMARGASAURUS, Body.SAUROPOD, 2.2f, 200, 12, 0.24f, 4, Diet.HERBIVORE, Temper.DOCILE, Size.LARGE,
                features(NECK_SPINES), N.neck(1.0f).tail(1.2f), 0x7A6A50, 0xC8C0A8, 0xA05A30, Pattern.STRIPES);
        add(Species.DREADNOUGHTUS, Body.SAUROPOD, 3.8f, 480, 22, 0.20f, 8, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(), N.neck(1.4f).tail(1.2f).body(1.15f), 0x6A7068, 0xB8C0B8, 0x4A5048, Pattern.STRIPES);
        add(Species.PROTOCERATOPS, Body.QUADRUPED, 0.8f, 40, 6, 0.28f, 2, Diet.HERBIVORE, Temper.DEFENSIVE, Size.SMALL,
                features(FRILL, BEAK), N.head(1.3f), 0xA08860, 0xE0D0B0, 0x705838, Pattern.NONE);
        add(Species.PACHYRHINOSAURUS, Body.QUADRUPED, 1.8f, 170, 15, 0.26f, 7, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(SPIKE_FRILL, NOSE_HORN, BEAK), N.head(1.2f), 0x7A6A50, 0xC8B898, 0x9A6040, Pattern.NONE);
        add(Species.SINOCERATOPS, Body.QUADRUPED, 1.9f, 180, 16, 0.26f, 7, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(SPIKE_FRILL, NOSE_HORN, BEAK), N.head(1.25f), 0x8A5A40, 0xD0B898, 0xC08040, Pattern.SPOTS);
        add(Species.EUOPLOCEPHALUS, Body.QUADRUPED, 1.6f, 200, 16, 0.20f, 13, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(ARMOR, TAIL_CLUB, BEAK), N.legs(0.75f).frontLegs(0.8f).body(1.2f).head(0.9f), 0x6A6A4A, 0xB0A888, 0x4A4A30, Pattern.SPOTS);
        add(Species.NODOSAURUS, Body.QUADRUPED, 1.5f, 190, 14, 0.20f, 12, Diet.HERBIVORE, Temper.DEFENSIVE, Size.LARGE,
                features(ARMOR, SHOULDER_SPIKES, BEAK), N.legs(0.75f).frontLegs(0.8f).body(1.2f), 0x7A6040, 0xB8A080, 0x503820, Pattern.SPOTS);
    }

    private static void add(Species species, Body body, float scale, float health, float damage, float speed, float armor,
                            Diet diet, Temper temper, Size size, java.util.Set<DinoSpec.Feature> features, Shape shape,
                            int base, int belly, int pattern, Pattern patternType) {
        SPECS.put(species, new DinoSpec(species, body, scale, health, damage, speed, armor, diet, temper, size, features, shape,
                base, belly, pattern, patternType));
    }

    /** The spec, or null for a species with no body yet. */
    @Nullable
    public static DinoSpec get(Species species) {
        return SPECS.get(species);
    }

    public static Iterable<DinoSpec> all() {
        return SPECS.values();
    }

    private DinoSpecs() {}
}
