package com.triassicisland.security;

import com.triassicisland.network.DataBlock;
import com.triassicisland.power.ElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A powered security device mounted on a wall, facing out (camera, keycard reader, alarm, monitor).
 * Takes power and data through any side but the front, e.g. from a hidden combined cable in the wall behind it.
 */
public abstract class WallDeviceBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
    private final BiFunction<BlockPos, BlockState, BlockEntity> factory;

    /** {@code depth} and {@code inset} in pixels: how far it sticks out of the wall, and its margin on the sides. */
    protected WallDeviceBlock(Properties properties, int depth, int inset, int bottom, int top, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties);
        this.factory = factory;
        shapes.put(Direction.NORTH, Block.box(inset, bottom, 16 - depth, 16 - inset, top, 16));
        shapes.put(Direction.SOUTH, Block.box(inset, bottom, 0, 16 - inset, top, depth));
        shapes.put(Direction.EAST, Block.box(0, bottom, inset, depth, top, 16 - inset));
        shapes.put(Direction.WEST, Block.box(16 - depth, bottom, inset, 16, top, 16 - inset));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean connectsOn(BlockState state, Direction face) {
        return face != state.getValue(FACING);
    }

    @Override
    public boolean connectsData(BlockState state, Direction face) {
        return face != state.getValue(FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof SecurityDeviceBlockEntity device) device.serverTick();
        };
    }
}
