package com.triassicisland.dino;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;

/** Hungry plant eaters with no feeder nearby eat grass, ferns and leaves (tall ones reach higher leaves). */
class GrazeGoal extends Goal {
    private final DinosaurEntity dino;
    private BlockPos food;
    private int timeout;

    GrazeGoal(DinosaurEntity dino) {
        this.dino = dino;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!dino.isHungry() || dino.getRandom().nextInt(40) != 0) return false;
        if (!ForgeEventFactory.getMobGriefingEvent(dino.level(), dino)) return false;
        food = find();
        return food != null;
    }

    private static boolean edible(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.GRASS_BLOCK);
    }

    private BlockPos find() {
        BlockPos origin = dino.blockPosition();
        int reachUp = Math.max(2, Math.round(dino.getBbHeight()));
        for (int attempt = 0; attempt < 40; attempt++) {
            BlockPos pos = origin.offset(dino.getRandom().nextInt(17) - 8, dino.getRandom().nextInt(reachUp + 2) - 1,
                    dino.getRandom().nextInt(17) - 8);
            if (edible(dino.level().getBlockState(pos))) return pos;
        }
        return null;
    }

    @Override
    public void start() {
        timeout = 200;
        dino.getNavigation().moveTo(food.getX() + 0.5, food.getY(), food.getZ() + 0.5, 0.9);
    }

    @Override
    public boolean canContinueToUse() {
        return food != null && timeout > 0;
    }

    @Override
    public void tick() {
        timeout--;
        dino.getLookControl().setLookAt(food.getX() + 0.5, food.getY() + 0.5, food.getZ() + 0.5);
        double reach = dino.getBbWidth() / 2 + 1.5 + dino.getBbHeight() / 3;
        if (dino.distanceToSqr(food.getX() + 0.5, food.getY() + 0.5, food.getZ() + 0.5) > reach * reach) {
            if (dino.getNavigation().isDone()) dino.getNavigation().moveTo(food.getX() + 0.5, food.getY(), food.getZ() + 0.5, 0.9);
            return;
        }
        BlockState state = dino.level().getBlockState(food);
        if (edible(state)) {
            if (state.is(Blocks.GRASS_BLOCK)) {
                dino.level().levelEvent(2001, food, net.minecraft.world.level.block.Block.getId(state));
                dino.level().setBlock(food, Blocks.DIRT.defaultBlockState(), 2);
            } else {
                dino.level().destroyBlock(food, false);
            }
            dino.eat(state.is(BlockTags.LEAVES) ? 8 : 5);
            dino.playSound(SoundEvents.GENERIC_EAT, 1, 0.6f);
        }
        food = null;
    }
}
