package com.triassicisland.light;

import com.triassicisland.power.SimpleElectricalBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class LampSupplyBlock extends SimpleElectricalBlock {
    public LampSupplyBlock(Properties properties) {
        super(properties, LampSupplyBlockEntity::new, Shapes.block());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (l, p, s, be) -> {
            if (be instanceof LampSupplyBlockEntity supply) supply.serverTick();
        };
    }
}
