package com.triassicisland.security;

import com.triassicisland.computer.Terminal;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * 15 W text monitor, 20 x 10 characters. Computers use write / print / clear / set_cursor / set_color.
 * Contents are synced to nearby players at most every 5 ticks.
 */
public class MonitorBlockEntity extends SecurityDeviceBlockEntity {
    public static final int COLUMNS = 20, ROWS = 10;
    private static final int SYNC_INTERVAL = 5;

    private final Terminal text = new Terminal(COLUMNS, ROWS);
    private int color = 0x33FF66;
    private boolean wasPowered;
    // Client copy.
    private String[] clientLines = new String[0];
    private int clientColor = 0x33FF66;
    private boolean clientPowered;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONITOR.get(), pos, state);
    }

    @Override
    protected double watts() {
        return 15;
    }

    @Override
    protected void serverTick() {
        boolean powered = isPowered();
        if (powered != wasPowered) {
            wasPowered = powered;
            text.markDirty();
        }
        if (level.getGameTime() % SYNC_INTERVAL == 0 && text.takeIfDirty() != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        super.addDeviceMethods(methods);
        methods.put("write", args -> {
            text.write(DeviceMethod.string(args, 0));
            return null;
        });
        methods.put("print", args -> {
            text.write(DeviceMethod.string(args, 0) + "\n");
            return null;
        });
        methods.put("clear", args -> {
            text.clear();
            return null;
        });
        methods.put("set_cursor", args -> {
            text.setCursor((int) DeviceMethod.number(args, 0), (int) DeviceMethod.number(args, 1));
            return null;
        });
        methods.put("size", args -> java.util.List.of(COLUMNS, ROWS));
        methods.put("set_color", args -> {
            color = Integer.parseInt(DeviceMethod.string(args, 0).replace("#", ""), 16) & 0xFFFFFF;
            text.markDirty();
            return null;
        });
    }

    // --- Client rendering data ---

    public String[] clientLines() {
        return clientLines;
    }

    public int clientColor() {
        return clientColor;
    }

    public boolean clientPowered() {
        return clientPowered;
    }

    private CompoundTag displayTag() {
        CompoundTag tag = new CompoundTag();
        ListTag lines = new ListTag();
        for (String line : text.peekLines()) lines.add(StringTag.valueOf(line));
        tag.put("Lines", lines);
        tag.putInt("Color", color);
        tag.putBoolean("Powered", isPowered());
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return displayTag();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, be -> ((MonitorBlockEntity) be).displayTag());
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) handleUpdateTag(packet.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        ListTag lines = tag.getList("Lines", 8);
        clientLines = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) clientLines[i] = lines.getString(i);
        clientColor = tag.getInt("Color");
        clientPowered = tag.getBoolean("Powered");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Color", color);
        ListTag lines = new ListTag();
        for (String line : text.peekLines()) lines.add(StringTag.valueOf(line));
        tag.put("Text", lines);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Color")) color = tag.getInt("Color");
        ListTag lines = tag.getList("Text", 8);
        for (int i = 0; i < lines.size() && i < ROWS; i++) {
            text.setCursor(0, i);
            text.write(lines.getString(i).stripTrailing());
        }
    }
}
