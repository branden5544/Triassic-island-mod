package com.triassicisland.light;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * An invisible light that removes itself unless something keeps refreshing it (flashlights, projectors).
 * Self-cleaning, so no stray lights are left behind after a crash or logout.
 */
public class TemporaryLightBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 15);
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> EXPIRY = new HashMap<>();
    private static final int CHECK_TICKS = 5;

    public TemporaryLightBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LEVEL)));
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 15));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    /** Places (or keeps) a light at {@code pos} for the next {@code ticks} ticks, if the spot is free. */
    public static void light(ServerLevel level, BlockPos pos, int brightness, int ticks, Block self) {
        BlockState current = level.getBlockState(pos);
        boolean ours = current.is(self);
        if (!ours && !current.isAir()) return;
        EXPIRY.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), level.getGameTime() + ticks);
        int clamped = Math.max(1, Math.min(15, brightness));
        if (!ours || current.getValue(LEVEL) != clamped) {
            level.setBlock(pos, self.defaultBlockState().setValue(LEVEL, clamped), Block.UPDATE_ALL);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        level.scheduleTick(pos, this, CHECK_TICKS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Map<BlockPos, Long> lights = EXPIRY.get(level.dimension());
        Long until = lights == null ? null : lights.get(pos);
        if (until == null || until < level.getGameTime()) {
            if (lights != null) lights.remove(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.scheduleTick(pos, this, CHECK_TICKS);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return true;
    }
}
