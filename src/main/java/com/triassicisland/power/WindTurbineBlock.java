package com.triassicisland.power;

import com.triassicisland.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The base of a wind turbine tower. Placing it builds the mast above it ({@link #HEIGHT} blocks in all);
 * breaking any part removes the whole tower. The mast and spinning rotor are drawn by a block entity renderer.
 */
public class WindTurbineBlock extends SimpleElectricalBlock {
    /** Total height including the base; the rotor hub is at the top. */
    public static final int HEIGHT = 5;

    public WindTurbineBlock(Properties properties) {
        super(properties, WindTurbineBlockEntity::new, Block.box(2, 0, 2, 14, 6, 14));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() + HEIGHT > level.getMaxBuildHeight()) return null;
        for (int i = 1; i < HEIGHT; i++) {
            if (!level.getBlockState(pos.above(i)).canBeReplaced(context)) return null;
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        for (int i = 1; i < HEIGHT; i++) {
            level.setBlock(pos.above(i), ModBlocks.WIND_TURBINE_MAST.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            for (int i = 1; i < HEIGHT; i++) {
                if (level.getBlockState(pos.above(i)).is(ModBlocks.WIND_TURBINE_MAST.get())) {
                    level.setBlock(pos.above(i), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
    }
}
