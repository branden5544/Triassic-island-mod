package com.triassicisland.power;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A 4:1 transformer (Electrical Age's DC-DC converter). The two sides are separate nodes in separate networks.
 *
 * <p>The output side is a voltage source at ratio x the input voltage. The input side is a load sized to draw
 * last tick's output power (plus losses). This one-tick lag keeps every network a simple resistor network
 * the solver can handle, while still conserving energy. Above its power rating it overheats and blows up.
 */
public class TransformerBlockEntity extends ElectricalBlockEntity {
    private static final double RATIO = 4;
    private static final double EFFICIENCY = 0.97;
    private static final double OUTPUT_RESISTANCE = 0.05;
    private static final double MAX_WATTS = 16_000;
    private static final int OVERLOAD_TICKS_TO_FAIL = 60;

    private final Node input = node;
    private final Node output = new Node(this);
    /** Output voltage target, smoothed so a weak input can't make it oscillate. */
    private double targetVoltage;
    private double outputWatts;
    private int overloadTicks;

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSFORMER.get(), pos, state);
    }

    protected TransformerBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** How many times the voltage is multiplied (step up) or divided (step down). */
    protected double magnitude() {
        return RATIO;
    }

    private Direction front() {
        return getBlockState().getValue(FacingElectricalBlock.FACING);
    }

    private double ratio() {
        return getBlockState().getValue(TransformerBlock.STEP_UP) ? magnitude() : 1 / magnitude();
    }

    @Override
    public List<Node> nodes() {
        return List.of(input, output);
    }

    @Nullable
    @Override
    public Node nodeAt(Direction face) {
        Direction front = front();
        if (face == front) return output;
        if (face == front.getOpposite()) return input;
        return null;
    }

    /**
     * The output only drives its network when it has something to give and the network isn't already higher
     * (e.g. a full battery). Like a diode, it never pulls the output network down.
     */
    private boolean outputActive() {
        return targetVoltage > 1 && output.voltage <= targetVoltage * 1.02;
    }

    @Override
    public double groundConductance(Node n) {
        if (n == output) return outputActive() ? 1 / OUTPUT_RESISTANCE : 0;
        double v = Math.max(1, input.voltage);
        return outputWatts / EFFICIENCY / (v * v);
    }

    @Override
    public double sourceCurrent(Node n) {
        return n == output && outputActive() ? targetVoltage / OUTPUT_RESISTANCE : 0;
    }

    @Override
    protected void onSolved(Node n) {
        if (n == input) {
            targetVoltage += (input.voltage * ratio() - targetVoltage) * 0.5;
        } else {
            // Supplying shows up as negative current.
            outputWatts = Math.max(0, -output.power());
            overloadTicks = outputWatts > MAX_WATTS ? overloadTicks + 1 : 0;
            if (overloadTicks >= OVERLOAD_TICKS_TO_FAIL) overvoltageFailure();
            setChanged();
        }
    }

    @Override
    public MutableComponent meterReading() {
        return Component.translatable("message.triassicisland.meter.input").append(reading(input))
                .append(Component.translatable("message.triassicisland.meter.output")).append(reading(output));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Target", targetVoltage);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        targetVoltage = tag.getDouble("Target");
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("input_voltage", args -> input.voltage);
        methods.put("output_voltage", args -> output.voltage);
        methods.put("output_power", args -> outputWatts);
        methods.put("is_step_up", args -> getBlockState().getValue(TransformerBlock.STEP_UP));
        methods.put("set_step_up", args -> {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(TransformerBlock.STEP_UP, DeviceMethod.bool(args, 0)));
            return null;
        });
    }
}
