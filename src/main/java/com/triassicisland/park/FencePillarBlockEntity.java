package com.triassicisland.park;

import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Just a conductor: links the fence blocks on either side. */
public class FencePillarBlockEntity extends ElectricalBlockEntity {
    public FencePillarBlockEntity(BlockPos pos, BlockState state) {
        super(ParkEntities.FENCE_PILLAR.get(), pos, state);
    }
}
