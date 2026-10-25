package com.triassicisland.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.GridManager;
import com.triassicisland.power.HasElectricalNode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * A Create kinetic block that is also a generator on the grid (through {@link Node}, since it can't extend
 * ElectricalBlockEntity). It can deliver up to 16 W per RPM - the stress it puts on the Create network, at 1 SU = 1 W -
 * as a voltage source whose internal resistance limits it to that.
 */
public class KineticGeneratorBlockEntity extends KineticBlockEntity implements HasElectricalNode {
    private Node node;

    public KineticGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    double voltage() {
        return getBlockState().getBlock() instanceof KineticGeneratorBlock block ? block.voltage : 50;
    }

    /** The most power the shaft can give right now, in watts. */
    double maxWatts() {
        if (isOverStressed()) return 0;
        return CreateCompat.IMPACT * Math.abs(getSpeed());
    }

    @Override
    public ElectricalBlockEntity electricalNode() {
        if (node == null) node = new Node(this);
        return node;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (level instanceof ServerLevel server) {
            electricalNode().setLevel(server);
            GridManager.attach(server, electricalNode());
        }
    }

    @Override
    public void remove() {
        if (level instanceof ServerLevel server && node != null) GridManager.detach(server, node);
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel server && node != null) GridManager.detach(server, node);
        super.onChunkUnloaded();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        double watts = node == null ? 0 : Math.abs(node.power());
        tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.triassicisland.kinetic_generator",
                String.format("%.0f", watts), String.format("%.0f", maxWatts()), (int) voltage())).withStyle(ChatFormatting.GOLD));
        return true;
    }

    /** The grid side of the generator. Never placed in the world itself; it lives at the generator's position. */
    static final class Node extends ElectricalBlockEntity {
        private final KineticGeneratorBlockEntity owner;

        Node(KineticGeneratorBlockEntity owner) {
            super(owner.getType(), owner.getBlockPos(), owner.getBlockState());
            this.owner = owner;
        }

        @Override
        public BlockState getBlockState() {
            return owner.getBlockState();
        }

        @Override
        protected double ratedVoltage() {
            return owner.voltage();
        }

        /** Norton equivalent of a source whose best possible output (at half voltage) is maxWatts. */
        @Override
        public double groundConductance() {
            double v = owner.voltage();
            return 4 * owner.maxWatts() / (v * v);
        }

        @Override
        public double sourceCurrent() {
            return groundConductance() * owner.voltage();
        }

        @Override
        protected void overvoltageFailure() {
            // Too much voltage from the grid side just stops it producing; Create blocks don't explode.
        }

        @Override
        public MutableComponent meterReading() {
            return super.meterReading().append(String.format("  |  shaft %.0f RPM, up to %.0f W", Math.abs(owner.getSpeed()), owner.maxWatts()));
        }
    }
}
