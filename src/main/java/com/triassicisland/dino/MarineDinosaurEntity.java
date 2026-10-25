package com.triassicisland.dino;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/** Marine reptiles: swim like dolphins, breathe underwater, and slowly dry out and get hurt on land. */
public class MarineDinosaurEntity extends DinosaurEntity {
    private static final int DRY_TICKS = 600;

    private int outOfWater;

    public MarineDinosaurEntity(EntityType<? extends DinosaurEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        lookControl = new SmoothSwimmingLookControl(this, 10);
        setPathfindingMalus(BlockPathTypes.WATER, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected Goal wanderGoal() {
        return new RandomSwimmingGoal(this, 1.0, 10);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            if (getTarget() == null) setDeltaMovement(getDeltaMovement().add(0, -0.005, 0));
        } else {
            super.travel(input);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (isInWaterRainOrBubble()) {
            outOfWater = 0;
        } else if (++outOfWater > DRY_TICKS && tickCount % 20 == 0) {
            hurt(damageSources().dryOut(), 2);
        }
    }
}
