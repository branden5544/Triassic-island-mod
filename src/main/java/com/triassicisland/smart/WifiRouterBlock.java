package com.triassicisland.smart;

import com.triassicisland.power.SimpleElectricalBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/** WiFi router: phones need one in range, and it links computers to wireless smart devices. */
public class WifiRouterBlock extends SimpleElectricalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public WifiRouterBlock(Properties properties) {
        super(properties, WifiRouterBlockEntity::new, Block.box(3, 0, 3, 13, 9, 13));
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof WifiRouterBlockEntity router) router.serverTick();
        };
    }
}
