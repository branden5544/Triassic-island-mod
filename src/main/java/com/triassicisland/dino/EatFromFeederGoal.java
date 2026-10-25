package com.triassicisland.dino;

import com.triassicisland.park.FeederBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

/** When hungry, walks to the nearest feeder with food it likes and eats until full or the feeder is empty. */
class EatFromFeederGoal extends Goal {
    private static final int CHUNK_RADIUS = 1;

    private final DinosaurEntity dino;
    private BlockPos feeder;
    private int cooldown;
    private int timeout;

    EatFromFeederGoal(DinosaurEntity dino) {
        this.dino = dino;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0 || !dino.isHungry() || dino.getTarget() != null) return false;
        cooldown = 60;
        feeder = findFeeder();
        return feeder != null;
    }

    private BlockPos findFeeder() {
        int cx = dino.blockPosition().getX() >> 4, cz = dino.blockPosition().getZ() >> 4;
        BlockPos best = null;
        double bestDist = 24 * 24;
        for (int x = cx - CHUNK_RADIUS; x <= cx + CHUNK_RADIUS; x++) {
            for (int z = cz - CHUNK_RADIUS; z <= cz + CHUNK_RADIUS; z++) {
                if (!dino.level().hasChunk(x, z)) continue;
                for (BlockEntity be : dino.level().getChunk(x, z).getBlockEntities().values()) {
                    if (!(be instanceof FeederBlockEntity f) || !hasFood(f)) continue;
                    double dist = be.getBlockPos().distSqr(dino.blockPosition());
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = be.getBlockPos();
                    }
                }
            }
        }
        return best;
    }

    private boolean hasFood(FeederBlockEntity feeder) {
        for (int i = 0; i < feeder.getContainerSize(); i++) {
            if (dino.likes(feeder.getItem(i))) return true;
        }
        return false;
    }

    @Override
    public void start() {
        timeout = 400;
        dino.getNavigation().moveTo(feeder.getX() + 0.5, feeder.getY(), feeder.getZ() + 0.5, 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        return feeder != null && timeout > 0 && dino.food() < DinosaurEntity.MAX_FOOD - 10;
    }

    @Override
    public void tick() {
        timeout--;
        dino.getLookControl().setLookAt(feeder.getX() + 0.5, feeder.getY() + 0.5, feeder.getZ() + 0.5);
        double reach = dino.getBbWidth() / 2 + 1.5;
        if (dino.distanceToSqr(feeder.getX() + 0.5, feeder.getY() + 0.5, feeder.getZ() + 0.5) > reach * reach) {
            if (dino.getNavigation().isDone()) dino.getNavigation().moveTo(feeder.getX() + 0.5, feeder.getY(), feeder.getZ() + 0.5, 1.0);
            return;
        }
        if (timeout % 20 != 0) return;
        if (!(dino.level().getBlockEntity(feeder) instanceof FeederBlockEntity f)) {
            feeder = null;
            return;
        }
        ItemStack eaten = f.takeFood(dino::likes);
        if (eaten.isEmpty()) {
            feeder = null;
            return;
        }
        dino.eat(DinosaurEntity.nutrition(eaten));
        dino.playSound(SoundEvents.GENERIC_EAT, 1, 0.7f);
        ((ServerLevel) dino.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten),
                feeder.getX() + 0.5, feeder.getY() + 0.7, feeder.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.05);
    }

    @Override
    public void stop() {
        feeder = null;
    }
}
