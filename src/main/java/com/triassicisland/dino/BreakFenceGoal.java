package com.triassicisland.dino;

import com.triassicisland.dino.DinoSpec.Feature;
import com.triassicisland.dino.DinoSpec.Size;
import com.triassicisland.dino.DinoSpec.Temper;
import com.triassicisland.park.ElectricFenceBlock;
import com.triassicisland.park.ElectricFenceBlockEntity;
import com.triassicisland.park.ElectricFenceGateBlock;
import com.triassicisland.park.FenceTier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;

/**
 * Escape behaviour. Hungry (or aggressive, or clever) dinosaurs look for a fence that isn't live and
 * smash through it. Clever species (raptors) also walk up and test live fences - and get shocked for it.
 */
class BreakFenceGoal extends Goal {
    private static final int RANGE = 10;

    private final DinosaurEntity dino;
    private BlockPos target;
    private boolean testing;
    private int cooldown = 100;
    private int timeout;

    BreakFenceGoal(DinosaurEntity dino) {
        this.dino = dino;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0) return false;
        cooldown = 100 + dino.getRandom().nextInt(100);
        if (dino.growth() < 0.5f || dino.fenceFear() > 0 || dino.getTarget() != null) return false;
        if (!ForgeEventFactory.getMobGriefingEvent(dino.level(), dino)) return false;
        DinoSpec spec = dino.spec();
        boolean clever = spec.has(Feature.CLEVER);
        boolean motivated = dino.isHungry()
                || (clever && dino.getRandom().nextInt(3) == 0)
                || (spec.temper() == Temper.AGGRESSIVE && dino.getRandom().nextInt(8) == 0);
        if (!motivated) return false;
        target = find(false);
        testing = false;
        if (target == null && clever && dino.getRandom().nextInt(2) == 0) {
            target = find(true);
            testing = target != null;
        }
        return target != null;
    }

    private boolean canBreak(FenceTier tier) {
        DinoSpec spec = dino.spec();
        if (spec.size() == Size.LARGE) return true;
        return tier == FenceTier.STANDARD && (spec.size() == Size.MEDIUM || spec.has(Feature.CLEVER));
    }

    /** Nearest breakable fence that's dead, or (when {@code live}) a live one to test. */
    private BlockPos find(boolean live) {
        BlockPos origin = dino.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RANGE, -2, -RANGE), origin.offset(RANGE, 3, RANGE))) {
            BlockState state = dino.level().getBlockState(pos);
            boolean fence = state.getBlock() instanceof ElectricFenceBlock;
            boolean gate = state.getBlock() instanceof ElectricFenceGateBlock && !state.getValue(ElectricFenceGateBlock.OPEN);
            if (!fence && !gate) continue;
            if (!(dino.level().getBlockEntity(pos) instanceof ElectricFenceBlockEntity electronics)) continue;
            FenceTier tier = fence ? ((ElectricFenceBlock) state.getBlock()).tier : FenceTier.HIGH_VOLTAGE;
            if (electronics.isEnergized() != live || (!live && !canBreak(tier))) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public void start() {
        timeout = 300;
        moveTo();
    }

    private void moveTo() {
        dino.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.2);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && timeout > 0 && dino.fenceFear() == 0
                && dino.level().getBlockEntity(target) instanceof ElectricFenceBlockEntity;
    }

    @Override
    public void tick() {
        timeout--;
        dino.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        double reach = dino.getBbWidth() / 2 + 1.8;
        if (dino.distanceToSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5) > reach * reach) {
            if (dino.getNavigation().isDone()) moveTo();
            return;
        }
        if (!(dino.level().getBlockEntity(target) instanceof ElectricFenceBlockEntity electronics)) return;
        if (electronics.isEnergized()) {
            // Touching a live fence hurts. That's what "testing" finds out.
            electronics.shock(dino);
        } else if (!testing) {
            dino.level().destroyBlock(target, true, dino);
            dino.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 1.5f, 0.6f);
            dino.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        target = null;
    }

    @Override
    public void stop() {
        target = null;
        dino.getNavigation().stop();
    }
}
