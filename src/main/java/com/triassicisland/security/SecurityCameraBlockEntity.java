package com.triassicisland.security;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A 10 W security camera. Viewed from a computer with the "cameras" program. */
public class SecurityCameraBlockEntity extends SecurityDeviceBlockEntity {
    public SecurityCameraBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SECURITY_CAMERA.get(), pos, state);
    }

    @Override
    protected double watts() {
        return 10;
    }
}
