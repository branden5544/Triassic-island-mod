package com.triassicisland.smart;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.Voltage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Map;

/**
 * Opens when a living thing (player or mob, not spectators) is within 2.5 blocks and closes a second after they
 * leave. Draws 5 W idle and 40 W while sliding. Without power it stays as it is.
 */
public class SlidingDoorBlockEntity extends ElectricalBlockEntity {
    public enum Mode { AUTO, OPEN, LOCKED }

    private static final double MIN_VOLTS = 35, SENSE_RANGE = 2.5;
    private static final int CLOSE_DELAY = 20, SLIDE_TICKS = 10;

    private Mode mode = Mode.AUTO;
    private int closeTimer;
    private int slideTicks;
    // Client-side animation, 0 = closed, 1 = open.
    private float slide, previousSlide;

    public SlidingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(SmartEntities.SLIDING_DOOR.get(), pos, state);
    }

    public boolean isPowered() {
        return Math.abs(voltage) >= MIN_VOLTS;
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.LOW;
    }

    @Override
    public double groundConductance() {
        return (slideTicks > 0 ? 40 : 5) / (Voltage.LOW * Voltage.LOW);
    }

    String cycleMode() {
        mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
        setChanged();
        return mode.name().toLowerCase(java.util.Locale.ROOT);
    }

    void serverTick() {
        if (slideTicks > 0) slideTicks--;
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 4 != 0 || !isPowered()) return;
        boolean open = getBlockState().getValue(DoorBlock.OPEN);
        boolean want = switch (mode) {
            case OPEN -> true;
            case LOCKED -> false;
            case AUTO -> someoneNear(server) || (open && closeTimer > 0);
        };
        if (mode == Mode.AUTO) closeTimer = someoneNear(server) ? CLOSE_DELAY : Math.max(0, closeTimer - 4);
        if (want != open) {
            SlidingDoorBlock.setOpenBoth(server, worldPosition, want);
            slideTicks = SLIDE_TICKS;
            server.playSound(null, worldPosition, want ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.35f, 1.6f);
        }
    }

    private boolean someoneNear(ServerLevel server) {
        AABB area = new AABB(worldPosition).expandTowards(0, 1, 0).inflate(SENSE_RANGE, 0.5, SENSE_RANGE);
        return !server.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && !e.isSpectator()).isEmpty();
    }

    void clientTick() {
        previousSlide = slide;
        float target = getBlockState().getValue(DoorBlock.OPEN) ? 1 : 0;
        slide += Math.signum(target - slide) * Math.min(Math.abs(target - slide), 1f / SLIDE_TICKS);
    }

    public float slide(float partialTick) {
        return previousSlide + (slide - previousSlide) * partialTick;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, 1, 0).inflate(1);
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_open", args -> getBlockState().getValue(DoorBlock.OPEN));
        methods.put("mode", args -> mode.name().toLowerCase(java.util.Locale.ROOT));
        methods.put("set_mode", args -> {
            mode = Mode.valueOf(DeviceMethod.string(args, 0).toUpperCase(java.util.Locale.ROOT));
            setChanged();
            return null;
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) Wifi.addWireless(this);
        BlockState state = getBlockState();
        slide = previousSlide = state.getValue(DoorBlock.OPEN) ? 1 : 0;
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel) Wifi.removeWireless(this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel) Wifi.removeWireless(this);
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Mode", mode.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            mode = Mode.valueOf(tag.getString("Mode"));
        } catch (IllegalArgumentException e) {
            mode = Mode.AUTO;
        }
    }
}
