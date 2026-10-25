package com.triassicisland.dino;

import com.triassicisland.TriassicIsland;
import com.triassicisland.dna.Species;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** One entity type per species (each has its own hitbox size), e.g. triassicisland:tyrannosaurus. */
@Mod.EventBusSubscriber(modid = TriassicIsland.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DinoEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TriassicIsland.MODID);
    private static final Map<Species, RegistryObject<EntityType<DinosaurEntity>>> TYPES = new EnumMap<>(Species.class);
    private static final Map<EntityType<?>, DinoSpec> SPECS = new IdentityHashMap<>();

    static {
        for (DinoSpec spec : DinoSpecs.all()) {
            String id = spec.species().id();
            EntityType.EntityFactory<DinosaurEntity> factory = spec.flies() ? FlyingDinosaurEntity::new
                    : spec.swims() ? MarineDinosaurEntity::new : DinosaurEntity::new;
            MobCategory category = spec.swims() ? MobCategory.WATER_CREATURE : MobCategory.CREATURE;
            TYPES.put(spec.species(), ENTITIES.register(id, () -> EntityType.Builder.of(factory, category)
                    .sized(spec.width(), spec.height())
                    .clientTrackingRange(10)
                    .build(TriassicIsland.id(id).toString())));
        }
    }

    @Nullable
    public static EntityType<DinosaurEntity> type(Species species) {
        RegistryObject<EntityType<DinosaurEntity>> type = TYPES.get(species);
        return type == null ? null : type.get();
    }

    public static Map<Species, RegistryObject<EntityType<DinosaurEntity>>> types() {
        return TYPES;
    }

    static synchronized DinoSpec spec(EntityType<?> type) {
        if (SPECS.isEmpty()) TYPES.forEach((species, entry) -> SPECS.put(entry.get(), DinoSpecs.get(species)));
        return SPECS.get(type);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        TYPES.forEach((species, type) -> {
            DinoSpec spec = DinoSpecs.get(species);
            event.put(type.get(), Mob.createMobAttributes()
                    .add(Attributes.FLYING_SPEED, spec.flies() ? 0.6 : 0.4)
                    .add(Attributes.MAX_HEALTH, spec.health())
                    .add(Attributes.MOVEMENT_SPEED, spec.speed())
                    .add(Attributes.ATTACK_DAMAGE, spec.damage())
                    .add(Attributes.ARMOR, spec.armor())
                    .add(Attributes.FOLLOW_RANGE, 32)
                    .add(Attributes.KNOCKBACK_RESISTANCE, spec.size() == DinoSpec.Size.LARGE ? 0.9 : 0.3)
                    .build());
        });
    }

    private DinoEntities() {}
}
