package com.triassicisland.smart;

import com.triassicisland.power.SimpleElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * A switch in a power line. Right-click to turn it on or off; computers can switch it over a data cable or
 * wirelessly through a WiFi router in range.
 */
public class SmartSwitchBlock extends SimpleElectricalBlock {
    public static final BooleanProperty ON = BooleanProperty.create("on");

    public SmartSwitchBlock(Properties properties) {
        super(properties, SmartSwitchBlockEntity::new, Shapes.block());
        registerDefaultState(stateDefinition.any().setValue(ON, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) set(level, pos, state, !state.getValue(ON));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static void set(Level level, BlockPos pos, BlockState state, boolean on) {
        if (state.getValue(ON) == on) return;
        level.setBlock(pos, state.setValue(ON, on), 3);
        level.playSound(null, pos, on ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.6f, 1.2f);
    }
}
