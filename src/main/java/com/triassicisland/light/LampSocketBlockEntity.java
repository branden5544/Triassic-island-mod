package com.triassicisland.light;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A socket's bulb: a resistor sized to the bulb's rated watts. Brightness follows power (voltage squared), economic
 * bulbs warm up, bulbs wear out while lit, and too much voltage blows them. Sockets with no wired power take it
 * from a nearby Lamp Supply instead.
 */
public class LampSocketBlockEntity extends ElectricalBlockEntity {
    private static final int OVERVOLTAGE_TICKS = 20;
    private static final int PROJECTOR_RANGE = 12;

    private ItemStack bulb = ItemStack.EMPTY;
    private int life;
    private int warm;
    private int overTicks;
    private int light;
    @Nullable
    private LampSupplyBlockEntity supply;
    private long supplyUntil;

    public LampSocketBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public BulbType bulbType() {
        return bulb.getItem() instanceof BulbItem item ? item.type : null;
    }

    public boolean hasBulb() {
        return !bulb.isEmpty();
    }

    void insert(ItemStack stack) {
        bulb = stack.copyWithCount(1);
        life = BulbItem.life(stack);
        warm = 0;
        overTicks = 0;
        updateState();
    }

    ItemStack remove() {
        ItemStack out = bulb.isEmpty() ? ItemStack.EMPTY : BulbItem.withLife(bulb.copy(), life);
        bulb = ItemStack.EMPTY;
        light = 0;
        updateState();
        return out;
    }

    private void destroyBulb(boolean blown) {
        bulb = ItemStack.EMPTY;
        light = 0;
        updateState();
        if (level instanceof ServerLevel server) {
            server.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, blown ? 1f : 0.5f, blown ? 1.2f : 1.6f);
            server.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    5, 0.15, 0.15, 0.15, 0.01);
        }
    }

    // --- Power ---

    @Override
    public double groundConductance() {
        BulbType type = bulbType();
        return type == null ? 0 : type.watts / (type.voltage * type.voltage);
    }

    /** Called by a Lamp Supply each time it scans; good for a couple of seconds. */
    void suppliedBy(LampSupplyBlockEntity supply, long until) {
        this.supply = supply;
        this.supplyUntil = until;
    }

    /** No wire to this socket: it can be powered wirelessly. */
    boolean isUnwired() {
        return Math.abs(voltage) < 1;
    }

    private double effectiveVoltage() {
        if (supply != null && !supply.isRemoved() && level != null && level.getGameTime() < supplyUntil && isUnwired()) {
            return supply.voltage();
        }
        return voltage;
    }

    protected void serverTick() {
        BulbType type = bulbType();
        if (type == null) {
            applyLight(0);
            return;
        }
        double v = Math.abs(effectiveVoltage());
        if (v > type.voltage * com.triassicisland.power.Voltage.OVERVOLTAGE_LIMIT) {
            if (++overTicks >= OVERVOLTAGE_TICKS) {
                destroyBulb(true);
                return;
            }
        } else {
            overTicks = 0;
        }
        double fraction = Math.min(1, (v / type.voltage) * (v / type.voltage));
        warm = fraction > 0.05 ? Math.min(Math.max(1, type.warmup), warm + 1) : 0;
        double warmth = type.warmup == 0 ? 1 : 0.3 + 0.7 * warm / type.warmup;
        int target = fraction < 0.05 ? 0 : (int) Math.round(type.light * fraction * warmth);
        if (target < 2) target = 0;
        if (target > 0 && --life <= 0) {
            destroyBulb(false);
            return;
        }
        applyLight(target);
        if (target > 0 && level instanceof ServerLevel server) {
            if (type.farming && server.getGameTime() % 20 == 0) helpCrops(server);
            if (getBlockState().getBlock() instanceof LampSocketBlock socket && socket.type.projector && server.getGameTime() % 10 == 0) {
                project(server, target);
            }
        }
    }

    /** Sets the brightness. The streetlight overrides this to light its lamp head instead. */
    protected void applyLight(int brightness) {
        light = brightness;
        BlockState state = getBlockState();
        if (state.hasProperty(LampSocketBlock.LIGHT) && state.getValue(LampSocketBlock.LIGHT) != brightness) {
            level.setBlock(worldPosition, state.setValue(LampSocketBlock.LIGHT, brightness), 3);
        }
    }

    private void updateState() {
        setChanged();
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(LampSocketBlock.HAS_BULB)) {
            level.setBlock(worldPosition, state.setValue(LampSocketBlock.HAS_BULB, hasBulb()).setValue(LampSocketBlock.LIGHT, light), 3);
        } else {
            applyLight(light);
        }
    }

    /** Farming bulbs give crops and saplings within 4 blocks extra growth ticks. */
    private void helpCrops(ServerLevel server) {
        for (int i = 0; i < 6; i++) {
            BlockPos pos = worldPosition.offset(server.random.nextInt(9) - 4, -server.random.nextInt(6), server.random.nextInt(9) - 4);
            BlockState state = server.getBlockState(pos);
            if (state.isRandomlyTicking() && state.getBlock() instanceof BonemealableBlock) state.randomTick(server, pos, server.random);
        }
    }

    /** Projector sockets throw light ahead of them. */
    private void project(ServerLevel server, int brightness) {
        Direction facing = getBlockState().getValue(LampSocketBlock.FACING);
        for (int d = 3; d <= PROJECTOR_RANGE; d += 3) {
            BlockPos pos = worldPosition.relative(facing, d);
            if (!server.getBlockState(pos).isAir() && !server.getBlockState(pos).is(ModBlocks.TEMPORARY_LIGHT.get())) break;
            TemporaryLightBlock.light(server, pos, brightness - d / 4, 30, ModBlocks.TEMPORARY_LIGHT.get());
        }
    }

    /** Long suspended lamps hang well below their block. */
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition).expandTowards(0, -2, 0).inflate(0.5);
    }

    @Override
    public MutableComponent meterReading() {
        BulbType type = bulbType();
        return super.meterReading().append(type == null ? "  |  no bulb"
                : String.format("  |  %s, life %d%%", type.id(), Math.round(100f * life / type.lifeTicks)));
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("has_bulb", args -> hasBulb());
        methods.put("bulb", args -> bulbType() == null ? null : bulbType().id());
        methods.put("life", args -> bulbType() == null ? 0.0 : (double) life / bulbType().lifeTicks);
        methods.put("light", args -> light);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!bulb.isEmpty()) tag.put("Bulb", bulb.save(new CompoundTag()));
        tag.putInt("BulbLife", life);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bulb = tag.contains("Bulb") ? ItemStack.of(tag.getCompound("Bulb")) : ItemStack.EMPTY;
        life = tag.getInt("BulbLife");
    }
}
