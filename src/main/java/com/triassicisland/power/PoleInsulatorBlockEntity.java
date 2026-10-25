package com.triassicisland.power;

import com.triassicisland.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Overhead wires to other insulators. Each wire has the resistance of the same length of cable of its tier,
 * carries up to 20 A, and burns through (dropping nothing) if overloaded for a few seconds or over its voltage.
 */
public class PoleInsulatorBlockEntity extends ElectricalBlockEntity {
    public static final int MAX_WIRES = 4;
    public static final double MAX_LENGTH = 32;
    private static final double BURN_HEAT = 100;

    public record Wire(BlockPos to, CableTier tier) {}

    private final List<Wire> wires = new ArrayList<>();
    private final Map<BlockPos, Double> heat = new HashMap<>();

    public PoleInsulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<Wire> wires() {
        return wires;
    }

    /** Where the wire attaches, in world coordinates. */
    public static Vec3 anchor(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5);
    }

    public static double length(BlockPos a, BlockPos b) {
        return anchor(a).distanceTo(anchor(b));
    }

    /** Coils used for a wire of this length: one per 16 blocks, at least one. */
    public static int coilsFor(double length) {
        return Math.max(1, (int) Math.ceil(length / 16));
    }

    public boolean isConnectedTo(BlockPos other) {
        return wires.stream().anyMatch(w -> w.to.equals(other));
    }

    public boolean hasRoom() {
        return wires.size() < MAX_WIRES;
    }

    /** Adds the wire on this end only; the caller connects the other end too. */
    public void addWire(BlockPos to, CableTier tier) {
        wires.add(new Wire(to.immutable(), tier));
        changed();
    }

    private void changed() {
        setChanged();
        if (level instanceof ServerLevel server) {
            GridManager.get(server).markDirty();
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Removes the wire at both ends. {@code drop} gives the coils back at this pole. */
    public void disconnect(Wire wire, boolean drop) {
        wires.remove(wire);
        heat.remove(wire.to);
        changed();
        if (level == null) return;
        if (level.getBlockEntity(wire.to) instanceof PoleInsulatorBlockEntity other) {
            other.wires.removeIf(w -> w.to.equals(worldPosition));
            other.changed();
        }
        if (drop) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    new net.minecraft.world.item.ItemStack(ModItems.wireCoil(wire.tier), coilsFor(length(worldPosition, wire.to))));
        }
    }

    public void disconnectAll(boolean drop) {
        for (Wire wire : List.copyOf(wires)) disconnect(wire, drop);
    }

    private void burn(Wire wire) {
        if (!(level instanceof ServerLevel server)) return;
        Vec3 a = anchor(worldPosition), b = anchor(wire.to);
        for (int i = 0; i <= 8; i++) {
            Vec3 p = a.lerp(b, i / 8.0);
            server.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.5f, 0.8f);
        disconnect(wire, false);
    }

    // --- Grid ---

    @Override
    public java.util.List<RemoteLink> remoteLinks(Node n) {
        List<RemoteLink> links = new ArrayList<>();
        for (Wire wire : wires) links.add(new RemoteLink(wire.to, CableTier.RESISTANCE * length(worldPosition, wire.to)));
        return links;
    }

    @Override
    public Node remoteNode() {
        return node;
    }

    @Override
    public void onRemoteCurrent(BlockPos to, double amps) {
        for (Wire wire : List.copyOf(wires)) {
            if (!wire.to.equals(to)) continue;
            double load = amps / CableTier.RATED_AMPS;
            double h = heat.getOrDefault(to, 0.0);
            h = load > 1 ? h + load * load - 1 : Math.max(0, h - 0.5);
            heat.put(to, h);
            boolean overvolted = Math.abs(voltage) > wire.tier.ratedVoltage * Voltage.OVERVOLTAGE_LIMIT;
            if (h >= BURN_HEAT || (overvolted && level != null && level.getGameTime() % 40 == 0)) burn(wire);
        }
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  %d wire(s)", wires.size()));
    }

    // --- Save and sync ---

    private ListTag wiresTag() {
        ListTag list = new ListTag();
        for (Wire wire : wires) {
            CompoundTag tag = NbtUtils.writeBlockPos(wire.to);
            tag.putString("Tier", wire.tier.name());
            list.add(tag);
        }
        return list;
    }

    private void readWires(ListTag list) {
        wires.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            try {
                wires.add(new Wire(NbtUtils.readBlockPos(tag), CableTier.valueOf(tag.getString("Tier"))));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Wires", wiresTag());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readWires(tag.getList("Wires", 10));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("Wires", wiresTag());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) readWires(packet.getTag().getList("Wires", 10));
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = new AABB(worldPosition);
        for (Wire wire : wires) box = box.minmax(new AABB(wire.to));
        return box.inflate(1);
    }
}
