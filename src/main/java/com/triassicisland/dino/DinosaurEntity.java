package com.triassicisland.dino;

import com.triassicisland.dino.DinoSpec.Diet;
import com.triassicisland.dino.DinoSpec.Temper;
import com.triassicisland.park.ElectricShock;
import com.triassicisland.park.FeederBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * A cloned dinosaur. Hatches small and grows to full size over {@link #GROW_TICKS}. Gets hungry, eats from
 * feeders (or grazes / hunts), and breaks out through fences that aren't live.
 */
public class DinosaurEntity extends PathfinderMob {
    private static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(DinosaurEntity.class, EntityDataSerializers.FLOAT);
    /** 30 minutes from hatchling to adult. */
    public static final int GROW_TICKS = 36_000;
    public static final int MAX_FOOD = 100, HUNGRY = 40;
    private static final int FOOD_DECAY_TICKS = 240;
    /** How long a shock keeps it away from fences. */
    private static final int FENCE_FEAR_TICKS = 2400;

    private DinoSpec spec;
    // Summoned dinosaurs start as adults; hatched ones are set to 0 by the egg.
    private int ageTicks = GROW_TICKS;
    private int food = 80;
    private int quality = 100;
    private int fenceFear;

    public DinosaurEntity(EntityType<? extends DinosaurEntity> type, Level level) {
        super(type, level);
        setMaxUpStep(1.0f + spec().scale() * 0.25f);
    }

    public DinoSpec spec() {
        if (spec == null) spec = DinoEntities.spec(getType());
        return spec;
    }

    // --- Growth ---

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GROWTH, 1f);
    }

    /** 0 = just hatched, 1 = adult. */
    public float growth() {
        return entityData.get(GROWTH);
    }

    /** Size multiplier from growth: hatchlings are 30% of adult size. */
    public float growthScale() {
        return 0.3f + 0.7f * growth();
    }

    public boolean isAdult() {
        return growth() >= 1;
    }

    /** Set age from growth (0-1), e.g. 0 for a fresh hatchling. */
    public void setGrowth(float growth) {
        ageTicks = Math.round(Math.max(0, Math.min(1, growth)) * GROW_TICKS);
        updateGrowth();
    }

    private void updateGrowth() {
        float growth = Math.min(1f, ageTicks / (float) GROW_TICKS);
        float healthFraction = getHealth() / getMaxHealth();
        entityData.set(GROWTH, growth);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(spec().health() * (0.25 + 0.75 * growth));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(spec().damage() * (0.3 + 0.7 * growth));
        setHealth(Math.max(1, healthFraction * getMaxHealth()));
        refreshDimensions();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GROWTH.equals(key)) refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(growthScale());
    }

    // --- Hunger ---

    public int food() {
        return food;
    }

    public boolean isHungry() {
        return food < HUNGRY;
    }

    public int quality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int fenceFear() {
        return fenceFear;
    }

    void eat(int amount) {
        food = Math.min(MAX_FOOD, food + amount);
        heal(amount / 10f);
    }

    /** Whether this species eats the item. */
    public boolean likes(ItemStack stack) {
        var props = stack.getFoodProperties(this);
        boolean meat = props != null && props.isMeat();
        boolean fish = stack.is(ItemTags.FISHES);
        return switch (spec().diet()) {
            case CARNIVORE -> meat || fish;
            case PISCIVORE -> fish || meat;
            case HERBIVORE -> !meat && !fish && FeederBlockEntity.isFood(stack);
        };
    }

    static int nutrition(ItemStack stack) {
        var props = stack.getItem().getFoodProperties();
        if (stack.is(Items.HAY_BLOCK)) return 40;
        return props == null ? 8 : Math.max(4, props.getNutrition() * 4);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        ageTicks++;
        if (ageTicks % 100 == 0 && growth() < 1) updateGrowth();
        if (tickCount % FOOD_DECAY_TICKS == 0 && food > 0) food--;
        if (food == 0 && tickCount % 100 == 0) hurt(damageSources().starve(), 1);
        if (fenceFear > 0) fenceFear--;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(ElectricShock.DAMAGE)) {
            fenceFear = FENCE_FEAR_TICKS;
            getNavigation().stop();
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity killed) {
        if (spec().isCarnivore()) eat(20 + Math.round(killed.getBbWidth() * killed.getBbHeight() * 15));
        return super.killedEntity(level, killed);
    }

    // --- Behaviour ---

    @Override
    protected void registerGoals() {
        DinoSpec spec = spec();
        goalSelector.addGoal(0, new FloatGoal(this));
        if (spec.temper() == Temper.DOCILE) {
            goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        } else {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        }
        if (!spec.flies() && !spec.swims()) goalSelector.addGoal(3, new BreakFenceGoal(this));
        goalSelector.addGoal(4, new EatFromFeederGoal(this));
        if (spec.diet() == Diet.HERBIVORE) goalSelector.addGoal(5, new GrazeGoal(this));
        goalSelector.addGoal(6, wanderGoal());
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        if (spec.temper() != Temper.DOCILE) targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (spec.temper() == Temper.AGGRESSIVE || spec.temper() == Temper.HUNTER) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                    target -> wantsToAttackPlayers()));
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                    this::isPrey));
        }
    }

    /** Walking species stroll; flyers and swimmers override this. */
    protected net.minecraft.world.entity.ai.goal.Goal wanderGoal() {
        return new WaterAvoidingRandomStrollGoal(this, 0.8);
    }

    private boolean wantsToAttackPlayers() {
        if (growth() < 0.3f) return false;
        return spec().temper() == Temper.AGGRESSIVE || food < 15;
    }

    private boolean isPrey(LivingEntity target) {
        if (!isHungry() || target instanceof Player || growth() < 0.3f) return false;
        if (target instanceof DinosaurEntity other) {
            return !other.spec().isCarnivore() && other.getBbWidth() * other.getBbHeight() < getBbWidth() * getBbHeight() * 0.6f;
        }
        if (spec().diet() == Diet.PISCIVORE && target instanceof AbstractFish) return true;
        return target instanceof Animal;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && likes(held)) {
            if (!level().isClientSide) {
                eat(nutrition(held));
                if (!player.getAbilities().instabuild) held.shrink(1);
                playSound(SoundEvents.GENERIC_EAT, 1, 0.8f);
                ((ServerLevel) level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, held.copyWithCount(1)),
                        getX(), getEyeY(), getZ(), 8, 0.3, 0.2, 0.3, 0.05);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (!level().isClientSide) player.displayClientMessage(status(), true);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public Component status() {
        String stage = growth() >= 1 ? "adult" : growth() >= 0.5f ? "juvenile" : "hatchling";
        return Component.empty()
                .append(spec().species().displayName().copy().withStyle(ChatFormatting.GOLD))
                .append(String.format("  %s %d%%  |  Health %.0f/%.0f  |  Food %d%%  |  DNA %d%%",
                        stage, Math.round(growth() * 100), getHealth(), getMaxHealth(), food, quality));
    }

    // --- Sounds and drops ---

    private boolean big() {
        return spec().scale() * growthScale() >= 1.6f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (spec().isCarnivore()) return big() ? SoundEvents.RAVAGER_ROAR : SoundEvents.FOX_SCREECH;
        return big() ? SoundEvents.RAVAGER_AMBIENT : SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return big() ? SoundEvents.RAVAGER_HURT : SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return big() ? SoundEvents.RAVAGER_DEATH : SoundEvents.FOX_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f + spec().scale() * growthScale() * 0.5f;
    }

    @Override
    public float getVoicePitch() {
        float size = Math.min(1, spec().scale() * growthScale() / 3f);
        return (1.5f - size) * (0.9f + random.nextFloat() * 0.2f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int size = Math.max(1, Math.round(spec().scale() * growthScale() * 2));
        spawnAtLocation(new ItemStack(Items.BEEF, Math.min(64, size + looting)));
        spawnAtLocation(new ItemStack(Items.BONE, Math.min(64, size)));
    }

    // --- Save ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DinoAge", ageTicks);
        tag.putInt("Food", food);
        tag.putInt("Quality", quality);
        tag.putInt("FenceFear", fenceFear);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ageTicks = tag.contains("DinoAge") ? tag.getInt("DinoAge") : GROW_TICKS;
        food = tag.contains("Food") ? tag.getInt("Food") : 80;
        quality = tag.contains("Quality") ? tag.getInt("Quality") : 100;
        fenceFear = tag.getInt("FenceFear");
        updateGrowth();
    }
}
