package com.triassicisland.security;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 5 W keycard reader. Keeps the last 10 swipes for computers to read. */
public class KeycardReaderBlockEntity extends SecurityDeviceBlockEntity {
    private static final int PULSE_TICKS = 40;
    private static final int HISTORY = 10;

    private int requiredLevel = 1;
    private int pulseTicks;
    private final ArrayDeque<Map<String, Object>> swipes = new ArrayDeque<>();

    public KeycardReaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KEYCARD_READER.get(), pos, state);
    }

    @Override
    protected double watts() {
        return 5;
    }

    int requiredLevel() {
        return requiredLevel;
    }

    int cycleRequiredLevel() {
        requiredLevel = requiredLevel % 5 + 1;
        setChanged();
        return requiredLevel;
    }

    void swipe(Player player, int cardLevel) {
        if (!isPowered()) {
            player.displayClientMessage(Component.translatable("message.triassicisland.reader.no_power"), true);
            return;
        }
        boolean granted = cardLevel >= requiredLevel;
        Map<String, Object> swipe = new LinkedHashMap<>();
        swipe.put("player", player.getGameProfile().getName());
        swipe.put("level", cardLevel);
        swipe.put("granted", granted);
        swipe.put("day_time", (int) (level.getDayTime() % 24000));
        swipes.addFirst(swipe);
        while (swipes.size() > HISTORY) swipes.removeLast();

        if (granted) {
            pulse(PULSE_TICKS);
            level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.BLOCKS, 1f, 1.5f);
        } else {
            level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BASS.get(), SoundSource.BLOCKS, 1f, 0.5f);
            player.displayClientMessage(Component.translatable("message.triassicisland.reader.denied", requiredLevel), true);
        }
    }

    private void pulse(int ticks) {
        pulseTicks = ticks;
        BlockState state = getBlockState();
        if (!state.getValue(KeycardReaderBlock.POWERED) && state.getBlock() instanceof KeycardReaderBlock reader) {
            reader.setPowered(level, worldPosition, state, true);
        }
    }

    @Override
    protected void serverTick() {
        if (pulseTicks > 0 && --pulseTicks == 0) {
            BlockState state = getBlockState();
            if (state.getBlock() instanceof KeycardReaderBlock reader) reader.setPowered(level, worldPosition, state, false);
        }
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        super.addDeviceMethods(methods);
        methods.put("required_level", args -> requiredLevel);
        methods.put("set_required_level", args -> {
            requiredLevel = Math.max(1, Math.min(5, (int) DeviceMethod.number(args, 0)));
            setChanged();
            return null;
        });
        methods.put("swipes", args -> new ArrayList<>(swipes));
        methods.put("pulse", args -> {
            pulse(args.isEmpty() ? PULSE_TICKS : (int) Math.max(1, Math.min(1200, DeviceMethod.number(args, 0) * 20)));
            return null;
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RequiredLevel", requiredLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        requiredLevel = Math.max(1, Math.min(5, tag.getInt("RequiredLevel")));
    }
}
