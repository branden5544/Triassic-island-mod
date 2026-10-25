package com.triassicisland.power;

import com.triassicisland.network.DeviceIds;
import com.triassicisland.network.DeviceMethod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A block on the electrical grid. Every node is tied to ground (0 V) through {@link #groundConductance()},
 * and sources push current into it ({@link #sourceCurrent()}): a load is a resistor to ground, a generator is a
 * voltage source with internal resistance (its Norton equivalent). Nodes on touching faces are linked through
 * their {@link #linkResistance()}. The {@link GridManager} solves all voltages once per tick and calls {@link #onSolved()}.
 *
 * <p>Single-node blocks override the no-argument methods. Blocks with several nodes (transformers) override the
 * {@link Node} versions and {@link #nodeAt}.
 */
public abstract class ElectricalBlockEntity extends BlockEntity {
    private static final int OVERVOLTAGE_TICKS_TO_FAIL = 40;

    protected final Node node = new Node(this);
    protected double voltage;
    /** Current flowing from this node to ground. Positive means it consumes power, negative means it supplies it. */
    protected double current;
    private int overvoltageTicks;
    /** Data network identity, assigned the first time a computer sees this device. */
    private String deviceId;
    private String label = "";

    protected ElectricalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- Topology ---

    public List<Node> nodes() {
        return List.of(node);
    }

    /** The node reachable through a face, or null if nothing connects there. */
    @Nullable
    public Node nodeAt(Direction face) {
        BlockState state = getBlockState();
        return state.getBlock() instanceof ElectricalBlock block && block.connectsOn(state, face) ? node : null;
    }

    /** A wire to a block that isn't touching this one (overhead lines). */
    public record RemoteLink(BlockPos to, double resistance) {}

    /** Wires from {@code n} to far-away blocks. Both ends must list each other. */
    public List<RemoteLink> remoteLinks(Node n) {
        return List.of();
    }

    /** The node a remote wire attaches to. */
    @Nullable
    public Node remoteNode() {
        return null;
    }

    /** Current through one of this block's remote wires (reported to the end with the lower position). */
    public void onRemoteCurrent(BlockPos to, double amps) {}

    // --- Single-node values ---

    /** Conductance from this node to ground, in siemens (1 / ohms). */
    public double groundConductance() {
        return 0;
    }

    /** Current injected into this node by a source, in amps. */
    public double sourceCurrent() {
        return 0;
    }

    /** Resistance from the centre of this block to its face, in ohms. A link between two blocks is the sum of both halves. */
    public double linkResistance() {
        return 0.001;
    }

    /** Hook for nodes to react to the new {@link #voltage} and {@link #current}. */
    protected void onSolved() {}

    /** The largest current through any link of this block this tick. Cables use it to heat up. */
    public void onLinkCurrent(double amps) {}

    /** The voltage this block is built for. Well above it, {@link #overvoltageFailure()} happens. 0 means no limit. */
    protected double ratedVoltage() {
        return 0;
    }

    // --- Per-node values (multi-node blocks override these) ---

    public double groundConductance(Node n) {
        return groundConductance();
    }

    public double sourceCurrent(Node n) {
        return sourceCurrent();
    }

    public double linkResistance(Node n) {
        return linkResistance();
    }

    protected void onSolved(Node n) {
        onSolved();
    }

    public void onLinkCurrent(Node n, double amps) {
        onLinkCurrent(amps);
    }

    /** Called by the solver with the voltage it found and the conductance/current the node offered. */
    final void solved(Node n, double voltage, double conductance, double injected) {
        n.voltage = voltage;
        n.current = voltage * conductance - injected;
        if (n == node) {
            this.voltage = n.voltage;
            this.current = n.current;
        }
        onSolved(n);
        if (n == node && ratedVoltage() > 0) {
            if (Math.abs(voltage) > ratedVoltage() * Voltage.OVERVOLTAGE_LIMIT) {
                if (++overvoltageTicks >= OVERVOLTAGE_TICKS_TO_FAIL) overvoltageFailure();
            } else {
                overvoltageTicks = 0;
            }
        }
    }

    /** By default the block overheats and blows up (without damaging terrain). */
    protected void overvoltageFailure() {
        if (level == null) return;
        BlockPos pos = worldPosition;
        level.destroyBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5f, Level.ExplosionInteraction.NONE);
    }

    public double voltage() {
        return voltage;
    }

    /** Power drawn from the grid in watts (negative when supplying). */
    public double power() {
        return voltage * current;
    }

    /** What the multimeter shows. */
    public MutableComponent meterReading() {
        return reading(node);
    }

    protected static MutableComponent reading(Node n) {
        return Component.literal(String.format("%.1f V  |  %.2f A  |  %.0f W", n.voltage, Math.abs(n.current), Math.abs(n.power())))
                .withStyle(ChatFormatting.YELLOW);
    }

    // --- Data network device ---

    /** The device type computers see, e.g. "circuit_breaker". */
    public String deviceType() {
        var key = ForgeRegistries.BLOCKS.getKey(getBlockState().getBlock());
        return key == null ? "device" : key.getPath();
    }

    public String deviceId() {
        if (deviceId == null && level instanceof ServerLevel server) {
            deviceId = DeviceIds.get(server.getServer()).next(deviceType());
            setChanged();
        }
        return deviceId == null ? "" : deviceId;
    }

    public String label() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label.length() > 32 ? label.substring(0, 32) : label;
        setChanged();
    }

    /** Everything a computer can call on this device. Server thread only. */
    public final Map<String, DeviceMethod> deviceMethods() {
        Map<String, DeviceMethod> methods = new LinkedHashMap<>();
        methods.put("voltage", args -> voltage);
        methods.put("current", args -> Math.abs(current));
        methods.put("power", args -> Math.abs(power()));
        methods.put("label", args -> label);
        methods.put("set_label", args -> {
            setLabel(DeviceMethod.string(args, 0));
            return null;
        });
        addDeviceMethods(methods);
        return methods;
    }

    /** Add device-specific methods (open/close, charge...). */
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {}

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (deviceId != null) tag.putString("DeviceId", deviceId);
        if (!label.isEmpty()) tag.putString("Label", label);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("DeviceId")) deviceId = tag.getString("DeviceId");
        label = tag.getString("Label");
    }

    // --- Grid membership ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) GridManager.get(server).add(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel server) GridManager.get(server).remove(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel server) GridManager.get(server).remove(this);
    }
}
