package com.triassicisland.power;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Put it in a cable run (front and back connect). Right-click to read it; sneak + right-click to reset. */
public class EnergyMeterBlock extends FacingElectricalBlock {
    public EnergyMeterBlock(Properties properties) {
        super(properties, EnergyMeterBlockEntity::new);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty() || !(level.getBlockEntity(pos) instanceof EnergyMeterBlockEntity meter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) meter.reset();
            player.displayClientMessage(Component.empty().append(meter.meterReading()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
