package com.triassicisland.light;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The streetlight's bulb lives in the base, but the light comes out of the head two blocks up. */
public class StreetLightBlockEntity extends LampSocketBlockEntity {
    public StreetLightBlockEntity(BlockPos pos, BlockState state) {
        super(LightEntities.STREET_LIGHT.get(), pos, state);
    }

    @Override
    protected void serverTick() {
        super.serverTick();
    }

    @Override
    protected void applyLight(int brightness) {
        if (level == null) return;
        BlockPos head = worldPosition.above(StreetLightBlock.HEIGHT - 1);
        BlockState state = level.getBlockState(head);
        if (state.is(LightBlocks.STREET_LIGHT_HEAD.get()) && state.getValue(LampSocketBlock.LIGHT) != brightness) {
            level.setBlock(head, state.setValue(LampSocketBlock.LIGHT, brightness), 3);
        }
    }
}
