package com.triassicisland.park;

import com.triassicisland.TriassicIsland;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Shocks from electric fences and gates. Damage and knockback grow with the voltage. */
public final class ElectricShock {
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, TriassicIsland.id("electric_shock"));
    /** Below this a fence is just a fence. */
    public static final double MIN_VOLTS = 100;

    /** Half-hearts of damage: 5 at 200 V, 14 at 800 V. */
    public static float damage(double volts) {
        return (float) (2 + volts * 0.015);
    }

    public static void shock(ServerLevel level, LivingEntity entity, BlockPos from, double volts) {
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE));
        if (!entity.hurt(source, damage(volts))) return;
        // Thrown back away from the fence.
        Vec3 away = entity.position().subtract(Vec3.atCenterOf(from)).multiply(1, 0, 1);
        if (away.lengthSqr() < 1e-4) away = new Vec3(1, 0, 0);
        double strength = 0.4 + volts / 1000;
        entity.knockback(strength, -away.x, -away.z);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                12, 0.3, 0.4, 0.3, 0.2);
        level.playSound(null, from, SoundEvents.BEE_STING, SoundSource.BLOCKS, 0.8f, 0.6f);
        level.playSound(null, from, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.8f);
    }

    public static boolean canBeShocked(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator()
                && !(entity instanceof net.minecraft.world.entity.player.Player player && player.isCreative());
    }

    private ElectricShock() {}
}
