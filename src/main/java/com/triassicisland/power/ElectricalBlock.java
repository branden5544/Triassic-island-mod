package com.triassicisland.power;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Blocks that join the grid. Cables connect to, and the grid links through, the faces where {@link #connectsOn} is true. */
public interface ElectricalBlock {
    default boolean connectsOn(BlockState state, Direction face) {
        return true;
    }
}
