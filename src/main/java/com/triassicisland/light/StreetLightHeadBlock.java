package com.triassicisland.light;

import com.triassicisland.power.TowerPartBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** The lamp at the top of a streetlight. */
public class StreetLightHeadBlock extends TowerPartBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public StreetLightHeadBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LampSocketBlock.LIGHT)), () -> LightBlocks.STREET_LIGHT.get(),
                StreetLightBlock.HEIGHT, Block.box(5, 0, 5, 11, 12, 11), true);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LampSocketBlock.LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LampSocketBlock.LIGHT);
    }
}
