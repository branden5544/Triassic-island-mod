package com.triassicisland.network;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Blocks on the data network. Cables pass data along; everything else is a device at the end of a cable. */
public interface DataBlock {
    default boolean connectsData(BlockState state, Direction face) {
        return true;
    }

    default boolean isDataCable() {
        return false;
    }
}
