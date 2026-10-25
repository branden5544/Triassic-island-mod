package com.triassicisland.security;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** 20 W siren, audible about 64 blocks away. */
public class AlarmBlockEntity extends SecurityDeviceBlockEntity {
    private static final int SOUND_INTERVAL = 50;

    private boolean redstone, remote;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALARM.get(), pos, state);
    }

    @Override
    protected double watts() {
        return sounding() ? 20 : 1;
    }

    private boolean sounding() {
        return (redstone || remote) && isPowered();
    }

    void setRedstone(boolean powered) {
        redstone = powered;
        setChanged();
    }

    @Override
    protected void serverTick() {
        boolean on = sounding();
        BlockState state = getBlockState();
        if (state.getValue(AlarmBlock.LIT) != on) level.setBlock(worldPosition, state.setValue(AlarmBlock.LIT, on), 3);
        if (on && level.getGameTime() % SOUND_INTERVAL == 0) {
            level.playSound(null, worldPosition, SoundEvents.RAID_HORN.get(), SoundSource.BLOCKS, 4f, 1.2f);
        }
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        super.addDeviceMethods(methods);
        methods.put("is_on", args -> sounding());
        methods.put("set_on", args -> {
            remote = DeviceMethod.bool(args, 0);
            setChanged();
            return null;
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Remote", remote);
        tag.putBoolean("Redstone", redstone);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        remote = tag.getBoolean("Remote");
        redstone = tag.getBoolean("Redstone");
    }
}
