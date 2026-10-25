package com.triassicisland.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Copper cable. Above its current rating it heats up, smokes, and finally burns; overvoltage burns it too. */
public class CableBlockEntity extends ElectricalBlockEntity {
    private static final double BURN_HEAT = 100;

    private double heat;
    private double amps;

    public CableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public double linkResistance() {
        return CableTier.RESISTANCE / 2;
    }

    @org.jetbrains.annotations.Nullable
    private CableTier tier() {
        return getBlockState().getBlock() instanceof CableCarrier cable ? cable.powerTier() : null;
    }

    /** Data-only cables (hidden ones have a block entity for their disguise) are not on the power grid. */
    @Override
    public java.util.List<Node> nodes() {
        return tier() == null ? java.util.List.of() : super.nodes();
    }

    @Override
    protected double ratedVoltage() {
        CableTier tier = tier();
        return tier == null ? 0 : tier.ratedVoltage;
    }

    @Override
    protected void overvoltageFailure() {
        burn();
    }

    @Override
    public void onLinkCurrent(double amps) {
        this.amps = amps;
        double load = amps / CableTier.RATED_AMPS;
        heat = load > 1 ? heat + load * load - 1 : Math.max(0, heat - 0.5);
        if (heat <= 0 || !(level instanceof ServerLevel server)) return;

        BlockPos pos = getBlockPos();
        if (server.getGameTime() % 5 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.01);
        }
        if (heat >= BURN_HEAT) burn();
    }

    private void burn() {
        if (!(level instanceof ServerLevel server)) return;
        BlockPos pos = getBlockPos();
        server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        server.destroyBlock(pos, false);
        server.setBlockAndUpdate(pos, BaseFireBlock.getState(server, pos));
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  cable %.1f / %.0f A, %.0f V", amps, CableTier.RATED_AMPS, ratedVoltage()));
    }
}
