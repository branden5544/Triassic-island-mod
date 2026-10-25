package com.triassicisland.light;

import com.triassicisland.power.ElectricalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Electrical Age's lamp supply: powers every unwired lamp socket within 16 blocks from its own grid connection,
 * so you don't have to run a cable to each lamp. The sockets get the supply's voltage.
 */
public class LampSupplyBlockEntity extends ElectricalBlockEntity {
    public static final int RANGE = 16;
    private static final int SCAN_TICKS = 40;

    private final List<LampSocketBlockEntity> sockets = new ArrayList<>();

    public LampSupplyBlockEntity(BlockPos pos, BlockState state) {
        super(LightEntities.LAMP_SUPPLY.get(), pos, state);
    }

    @Override
    public double groundConductance() {
        double g = 0;
        for (LampSocketBlockEntity socket : sockets) {
            BulbType type = socket.bulbType();
            if (type != null && !socket.isRemoved()) g += type.watts / (type.voltage * type.voltage);
        }
        return g;
    }

    void serverTick() {
        if (level == null || level.getGameTime() % SCAN_TICKS != 0) return;
        sockets.clear();
        int cx = worldPosition.getX() >> 4, cz = worldPosition.getZ() >> 4;
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                if (!level.hasChunk(x, z)) continue;
                for (BlockEntity be : level.getChunk(x, z).getBlockEntities().values()) {
                    if (be instanceof LampSocketBlockEntity socket && socket.isUnwired()
                            && be.getBlockPos().distSqr(worldPosition) <= RANGE * RANGE) {
                        socket.suppliedBy(this, level.getGameTime() + SCAN_TICKS + 5);
                        sockets.add(socket);
                    }
                }
            }
        }
    }

    @Override
    public MutableComponent meterReading() {
        return super.meterReading().append(String.format("  |  powering %d lamp(s)", sockets.size()));
    }
}
