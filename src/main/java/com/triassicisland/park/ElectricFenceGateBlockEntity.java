package com.triassicisland.park;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricFenceGateBlockEntity extends ElectricFenceBlockEntity {
    private static final double OPEN_RESISTANCE = 1e9;

    public ElectricFenceGateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FENCE_GATE.get(), pos, state);
    }

    private boolean open() {
        return getBlockState().getValue(ElectricFenceGateBlock.OPEN);
    }

    @Override
    protected boolean canShock() {
        return !open();
    }

    @Override
    public double linkResistance() {
        return open() ? OPEN_RESISTANCE : super.linkResistance();
    }

    @Override
    protected void addDeviceMethods(java.util.Map<String, com.triassicisland.network.DeviceMethod> methods) {
        super.addDeviceMethods(methods);
        methods.put("is_open", args -> open());
        methods.put("open", args -> {
            ElectricFenceGateBlock.setOpen(level, worldPosition, getBlockState(), true);
            return null;
        });
        methods.put("close", args -> {
            ElectricFenceGateBlock.setOpen(level, worldPosition, getBlockState(), false);
            return null;
        });
    }
}
