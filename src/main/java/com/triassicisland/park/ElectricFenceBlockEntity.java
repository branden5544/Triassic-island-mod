package com.triassicisland.park;

import java.util.Map;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Fence electronics shared by fence panels and gates: idle power draw, the energized check,
 * and shocking anything that touches it (which briefly draws a lot of power).
 */
public class ElectricFenceBlockEntity extends ElectricalBlockEntity {
    private static final int CHECK_INTERVAL = 5;
    private static final double SHOCK_WATTS = 1500;
    private static final int SHOCK_TICKS = 4;

    private int shockTicks;

    public ElectricFenceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FENCE.get(), pos, state);
    }

    protected ElectricFenceBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected FenceTier tier() {
        return getBlockState().getBlock() instanceof ElectricFenceBlock fence ? fence.tier : FenceTier.HIGH_VOLTAGE;
    }

    /** Whether it's live enough to shock. Dinosaurs will check this to decide whether they dare to break out. */
    public boolean isEnergized() {
        return voltage >= ElectricShock.MIN_VOLTS && canShock();
    }

    /** Gates can't shock while they're open. */
    protected boolean canShock() {
        return true;
    }

    @Override
    protected double ratedVoltage() {
        return tier().ratedVoltage;
    }

    @Override
    public double groundConductance() {
        double watts = tier().idleWatts + (shockTicks > 0 ? SHOCK_WATTS : 0);
        return watts / (tier().ratedVoltage * tier().ratedVoltage);
    }

    /** Fences just break rather than exploding. */
    @Override
    protected void overvoltageFailure() {
        if (level != null) level.destroyBlock(worldPosition, false);
    }

    void serverTick() {
        if (shockTicks > 0) shockTicks--;
        if (!(level instanceof ServerLevel server)) return;
        // Spread the checks out so a long fence doesn't do them all on the same tick.
        if ((server.getGameTime() + worldPosition.asLong()) % CHECK_INTERVAL != 0) return;

        boolean energized = isEnergized();
        BlockState state = getBlockState();
        if (state.hasProperty(ElectricFenceBlock.ENERGIZED) && state.getValue(ElectricFenceBlock.ENERGIZED) != energized) {
            server.setBlock(worldPosition, state.setValue(ElectricFenceBlock.ENERGIZED, energized), 2);
        }
        if (!energized) return;

        AABB reach = getBlockState().getShape(server, worldPosition).bounds().move(worldPosition).inflate(0.1);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, reach, ElectricShock::canBeShocked)) {
            ElectricShock.shock(server, entity, worldPosition, voltage);
            shockTicks = SHOCK_TICKS;
        }
    }

    /** Shock a single entity, e.g. a player who grabs an energized gate. */
    public void shock(LivingEntity entity) {
        if (level instanceof ServerLevel server && isEnergized()) {
            ElectricShock.shock(server, entity, worldPosition, voltage);
            shockTicks = SHOCK_TICKS;
        }
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_energized", args -> isEnergized());
    }
}
