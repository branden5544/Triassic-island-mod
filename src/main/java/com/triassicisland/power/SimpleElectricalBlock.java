package com.triassicisland.power;

import com.triassicisland.network.DataBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A grid block with no GUI (battery, solar panel...). Also a device on the data network. */
public class SimpleElectricalBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock {
    private final BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory;
    private final VoxelShape shape;

    public SimpleElectricalBlock(Properties properties, BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory, VoxelShape shape) {
        super(properties);
        this.factory = factory;
        this.shape = shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.create(pos, state);
    }
}
