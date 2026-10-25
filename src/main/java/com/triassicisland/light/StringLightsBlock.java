package com.triassicisland.light;

import com.triassicisland.power.CableBlock;
import com.triassicisland.power.CableTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

/** Decorative 50 V string lights. They connect and carry power like a low voltage cable, and each block glows. */
public class StringLightsBlock extends CableBlock {
    public StringLightsBlock(Properties properties) {
        super(CableTier.LOW, false, properties.lightLevel(state -> state.getValue(LampSocketBlock.LIGHT)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LampSocketBlock.LIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StringLightsBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof StringLightsBlockEntity lights) lights.serverTick();
        };
    }
}
