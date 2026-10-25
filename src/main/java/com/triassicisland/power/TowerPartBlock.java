package com.triassicisland.power;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Part of a multi-block tower above its base block (wind turbine mast, streetlight pole). Breaking it breaks the
 * whole tower; right-clicking it uses the base. Invisible ones are drawn by the base's renderer or model.
 */
public class TowerPartBlock extends Block {
    private final Supplier<? extends Block> base;
    private final int height;
    private final VoxelShape shape;
    private final boolean visible;

    public TowerPartBlock(Properties properties, Supplier<? extends Block> base, int height, VoxelShape shape, boolean visible) {
        super(properties);
        this.base = base;
        this.height = height;
        this.shape = shape;
        this.visible = visible;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return visible ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    private BlockPos base(BlockGetter level, BlockPos pos) {
        for (int i = 1; i < height; i++) {
            if (level.getBlockState(pos.below(i)).is(base.get())) return pos.below(i);
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        super.onRemove(state, level, pos, newState, moving);
        if (state.is(newState.getBlock())) return;
        BlockPos base = base(level, pos);
        if (base != null) level.destroyBlock(base, true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos base = base(level, pos);
        if (base == null) return InteractionResult.PASS;
        BlockState baseState = level.getBlockState(base);
        return baseState.use(level, player, hand, new BlockHitResult(hit.getLocation(), hit.getDirection(), base, false));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(base.get());
    }
}
