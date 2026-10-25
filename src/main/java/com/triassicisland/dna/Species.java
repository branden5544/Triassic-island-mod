package com.triassicisland.dna;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Optional;

/** Every creature that can be cloned. Stored on items by its lowercase id. */
public enum Species {
    // Theropods
    TYRANNOSAURUS("T. rex"), VELOCIRAPTOR("Velociraptor"), DILOPHOSAURUS("Dilophosaurus"),
    SPINOSAURUS("Spinosaurus"), ALLOSAURUS("Allosaurus"), CARNOTAURUS("Carnotaurus"), BARYONYX("Baryonyx"),
    COMPSOGNATHUS("Compsognathus"), GIGANOTOSAURUS("Giganotosaurus"), CERATOSAURUS("Ceratosaurus"),
    COELOPHYSIS("Coelophysis"), HERRERASAURUS("Herrerasaurus"), UTAHRAPTOR("Utahraptor"),
    GALLIMIMUS("Gallimimus"), THERIZINOSAURUS("Therizinosaurus"), OVIRAPTOR("Oviraptor"),
    // Sauropods
    BRACHIOSAURUS("Brachiosaurus"), APATOSAURUS("Apatosaurus"), DIPLODOCUS("Diplodocus"),
    PLATEOSAURUS("Plateosaurus"), MAMENCHISAURUS("Mamenchisaurus"),
    // Ornithischians
    TRICERATOPS("Triceratops"), STEGOSAURUS("Stegosaurus"), ANKYLOSAURUS("Ankylosaurus"),
    PARASAUROLOPHUS("Parasaurolophus"), PACHYCEPHALOSAURUS("Pachycephalosaurus"), EDMONTOSAURUS("Edmontosaurus"),
    IGUANODON("Iguanodon"), STYRACOSAURUS("Styracosaurus"), KENTROSAURUS("Kentrosaurus"),
    // Flyers
    PTERANODON("Pteranodon"), DIMORPHODON("Dimorphodon"), QUETZALCOATLUS("Quetzalcoatlus"),
    // Marine
    MOSASAURUS("Mosasaurus"), PLESIOSAURUS("Plesiosaurus"), ICHTHYOSAURUS("Ichthyosaurus"), LIOPLEURODON("Liopleurodon"),
    // Added later; kept at the end so earlier species keep their order.
    DEINONYCHUS("Deinonychus"), TROODON("Troodon"), SUCHOMIMUS("Suchomimus"), MAJUNGASAURUS("Majungasaurus"),
    CONCAVENATOR("Concavenator"), METRIACANTHOSAURUS("Metriacanthosaurus"), MEGALOSAURUS("Megalosaurus"),
    PYRORAPTOR("Pyroraptor"), PROCERATOSAURUS("Proceratosaurus"),
    CORYTHOSAURUS("Corythosaurus"), LAMBEOSAURUS("Lambeosaurus"), MAIASAURA("Maiasaura"), OURANOSAURUS("Ouranosaurus"),
    HYPSILOPHODON("Hypsilophodon"), STYGIMOLOCH("Stygimoloch"),
    ARGENTINOSAURUS("Argentinosaurus"), CAMARASAURUS("Camarasaurus"), AMARGASAURUS("Amargasaurus"), DREADNOUGHTUS("Dreadnoughtus"),
    PROTOCERATOPS("Protoceratops"), PACHYRHINOSAURUS("Pachyrhinosaurus"), SINOCERATOPS("Sinoceratops"),
    EUOPLOCEPHALUS("Euoplocephalus"), NODOSAURUS("Nodosaurus");

    private static final String TAG_SPECIES = "Species";
    private static final String TAG_QUALITY = "Quality";

    private final String englishName;

    Species(String englishName) {
        this.englishName = englishName;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component displayName() {
        return Component.translatableWithFallback("species.triassicisland." + id(), englishName);
    }

    public static Species random(RandomSource random) {
        return values()[random.nextInt(values().length)];
    }

    // --- Genetic data stored on item stacks ---

    public static Optional<Species> of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SPECIES)) return Optional.empty();
        try {
            return Optional.of(valueOf(tag.getString(TAG_SPECIES).toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** DNA quality in percent (0-100). */
    public static int quality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_QUALITY);
    }

    public ItemStack apply(ItemStack stack) {
        stack.getOrCreateTag().putString(TAG_SPECIES, id());
        return stack;
    }

    public ItemStack apply(ItemStack stack, int quality) {
        apply(stack).getOrCreateTag().putInt(TAG_QUALITY, Math.max(0, Math.min(100, quality)));
        return stack;
    }
}
