package com.triassicisland.light;

import com.triassicisland.network.DataBlock;
import com.triassicisland.power.ElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A lamp socket. Right-click with a bulb to fit it, with an empty hand to take it out.
 * FACING is the way the lamp points (away from the surface it's mounted on).
 */
public class LampSocketBlock extends BaseEntityBlock implements ElectricalBlock, DataBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty HAS_BULB = BooleanProperty.create("has_bulb");

    public final SocketType type;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public LampSocketBlock(SocketType type, Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIGHT)));
        this.type = type;
        for (Direction dir : Direction.values()) shapes.put(dir, rotate(type.box, dir));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(LIGHT, 0).setValue(HAS_BULB, false));
    }

    /** Rotates a box given pointing up (mounted on the floor) to point {@code facing}. Matches the blockstate rotations. */
    public static VoxelShape rotate(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        return switch (facing) {
            case UP -> Block.box(x1, y1, z1, x2, y2, z2);
            case DOWN -> Block.box(x1, 16 - y2, 16 - z2, x2, 16 - y1, 16 - z1);
            case NORTH -> Block.box(x1, z1, 16 - y2, x2, z2, 16 - y1);
            case SOUTH -> Block.box(16 - x2, z1, y1, 16 - x1, z2, y2);
            case EAST -> Block.box(y1, z1, x1, y2, z2, x2);
            case WEST -> Block.box(16 - y2, z1, 16 - x2, 16 - y1, z2, 16 - x1);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIGHT, HAS_BULB);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (type.ceilingOnly && facing != Direction.DOWN) return null;
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return useSocket(level, pos, player, hand);
    }

    /** Fit or take out a bulb. Shared with the streetlight. */
    static InteractionResult useSocket(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof LampSocketBlockEntity socket)) return InteractionResult.PASS;
        var held = player.getItemInHand(hand);
        if (held.getItem() instanceof BulbItem && !socket.hasBulb()) {
            if (!level.isClientSide) {
                socket.insert(held);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && socket.hasBulb()) {
            if (!level.isClientSide) player.getInventory().placeItemBackInInventory(socket.remove());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && !level.isClientSide) player.displayClientMessage(Component.translatable("message.triassicisland.socket.empty"), true);
        return held.isEmpty() ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LampSocketBlockEntity socket && socket.hasBulb()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, socket.remove());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LampSocketBlockEntity(LightEntities.LAMP_SOCKET.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof LampSocketBlockEntity socket) socket.serverTick();
        };
    }
}
