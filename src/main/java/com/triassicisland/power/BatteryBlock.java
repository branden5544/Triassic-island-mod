package com.triassicisland.power;

import com.triassicisland.light.FlashlightItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

public class BatteryBlock extends SimpleElectricalBlock {
    public final BatteryBlockEntity.Size size;

    public BatteryBlock(BatteryBlockEntity.Size size, Properties properties) {
        super(properties, BatteryBlockEntity::new, Shapes.block());
        this.size = size;
    }

    /** Right-click with a flashlight to recharge it from the battery. */
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof FlashlightItem flashlight) || !(level.getBlockEntity(pos) instanceof BatteryBlockEntity battery)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            int missing = flashlight.capacity - flashlight.charge(held);
            // A flashlight uses about 2 W.
            int got = (int) (battery.drain(missing * 2.0 / 20) * 20 / 2);
            flashlight.setCharge(held, flashlight.charge(held) + got);
            player.displayClientMessage(Component.translatable("message.triassicisland.flashlight.charged",
                    Math.round(100f * flashlight.charge(held) / flashlight.capacity)), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
