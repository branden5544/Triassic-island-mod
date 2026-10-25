package com.triassicisland.security;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** A siren. Sounds while it gets a redstone signal or a computer turns it on. */
public class AlarmBlock extends WallDeviceBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AlarmBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? 10 : 0), 5, 4, 4, 12, AlarmBlockEntity::new);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AlarmBlockEntity alarm) {
            alarm.setRedstone(level.hasNeighborSignal(pos));
        }
    }
}
