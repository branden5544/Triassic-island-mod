package com.triassicisland.power;

import com.triassicisland.network.DataBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** What a cable block carries: power at some voltage tier, data, or both. Shared by visible and hidden cables. */
public interface CableCarrier {
    @Nullable
    CableTier powerTier();

    boolean carriesData();

    /** Whether a cable carrying {@code self}'s contents should reach out to {@code neighbor} in direction {@code dir}. */
    static boolean attaches(CableCarrier self, BlockState neighbor, Direction dir) {
        Direction face = dir.getOpposite();
        if (self.powerTier() != null && neighbor.getBlock() instanceof ElectricalBlock electrical && electrical.connectsOn(neighbor, face)) {
            return true;
        }
        return self.carriesData() && neighbor.getBlock() instanceof DataBlock data && data.connectsData(neighbor, face);
    }
}
