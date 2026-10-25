package com.triassicisland.computer;

import com.triassicisland.ModConfig;
import com.triassicisland.network.DataNetwork;
import com.triassicisland.network.DeviceMethod;
import com.triassicisland.packet.CameraViewPacket;
import com.triassicisland.packet.EditorOpenPacket;
import com.triassicisland.packet.ModNetwork;
import com.triassicisland.packet.TerminalOpenPacket;
import com.triassicisland.packet.TerminalUpdatePacket;
import com.triassicisland.power.ElectricalBlockEntity;
import com.triassicisland.power.Voltage;
import com.triassicisland.registry.ModBlockEntities;
import com.triassicisland.security.SecurityCameraBlockEntity;
import com.triassicisland.security.WallDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A computer: a 30 W load on the grid that runs Python while it has at least 35 V. On a brownout the program
 * dies; when power comes back a computer that was on boots again (running startup.py if it has one).
 */
public class ComputerBlockEntity extends ElectricalBlockEntity {
    private static final double WATTS = 30;
    private static final double MIN_VOLTS = 35;
    private static final double VIEW_DISTANCE_SQ = 8 * 8;

    final Terminal terminal = new Terminal();
    private final Set<UUID> viewers = new HashSet<>();
    private ScriptRunner runner;
    private boolean on;
    private boolean warnedDisabled;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER.get(), pos, state);
    }

    @Override
    protected double ratedVoltage() {
        return Voltage.LOW;
    }

    @Override
    public double groundConductance() {
        return on ? WATTS / (Voltage.LOW * Voltage.LOW) : 0;
    }

    public boolean hasPower() {
        return voltage >= MIN_VOLTS;
    }

    void serverTick() {
        if (!(level instanceof ServerLevel server)) return;
        boolean powered = voltage >= MIN_VOLTS;
        if (runner != null) {
            if (!powered) {
                stopRunner();
                terminal.clear();
                terminal.write("Power failure.\n");
            } else if (runner.finished()) {
                // The BIOS returned on its own (the shell was exited).
                runner = null;
                on = false;
                setChanged();
            } else {
                runner.serverTick();
            }
        } else if (on && powered) {
            if (ModConfig.COMPUTERS_ENABLED.get()) {
                boot(server);
            } else if (!warnedDisabled) {
                warnedDisabled = true;
                terminal.clear();
                terminal.write("Computers are disabled on this server.\n");
            }
        } else if (on && !powered && server.getGameTime() % 40 == 0) {
            terminal.clear();
            terminal.write(String.format("No power (%.0f V). Needs 50 V.\n", voltage));
        }
        if (server.getGameTime() % 2 == 0) syncViewers(server);
    }

    private void boot(ServerLevel server) {
        terminal.clear();
        terminal.write("Booting...\n");
        runner = new ScriptRunner(this, server.getServer(), files(server));
        runner.start();
    }

    private ComputerFiles files(ServerLevel server) {
        return new ComputerFiles(server.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("triassicisland").resolve("computers").resolve(deviceId()));
    }

    private void stopRunner() {
        if (runner != null) {
            runner.kill();
            runner = null;
        }
    }

    public void turnOn() {
        if (!on) {
            on = true;
            setChanged();
        }
    }

    public void shutdown() {
        stopRunner();
        on = false;
        terminal.clear();
        terminal.write("Shut down. Right-click or press Power to start.\n");
        setChanged();
    }

    public void reboot() {
        stopRunner();
        on = true;
        setChanged();
    }

    // --- Terminal viewers ---

    public void addViewer(ServerPlayer player) {
        viewers.add(player.getUUID());
        ModNetwork.toPlayer(player, new TerminalOpenPacket(worldPosition));
        terminal.markDirty();
    }

    public void removeViewer(ServerPlayer player) {
        viewers.remove(player.getUUID());
    }

    public boolean canUse(ServerPlayer player) {
        return !isRemoved() && player.distanceToSqr(worldPosition.getCenter()) <= VIEW_DISTANCE_SQ;
    }

    private void syncViewers(ServerLevel server) {
        String[] lines = terminal.takeIfDirty();
        if (lines == null) return;
        int[] cursor = terminal.cursor();
        TerminalUpdatePacket packet = new TerminalUpdatePacket(worldPosition, lines, cursor[0], cursor[1], runner != null);
        viewers.removeIf(id -> {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(id);
            if (player == null || !canUse(player)) return true;
            ModNetwork.toPlayer(player, packet);
            return false;
        });
    }

    public void handleInput(String line) {
        if (runner != null) runner.input.add(line);
    }

    public void handleAction(ServerPlayer player, String action) {
        switch (action) {
            case "terminate" -> {
                if (runner != null) runner.terminate();
            }
            case "reboot" -> reboot();
            case "power" -> {
                if (on) shutdown();
                else turnOn();
            }
            case "close" -> removeViewer(player);
            default -> {}
        }
    }

    // --- Editor ---

    void openEditor(String name, String text) {
        if (!(level instanceof ServerLevel server)) return;
        for (UUID id : viewers) {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(id);
            if (player != null) ModNetwork.toPlayer(player, new EditorOpenPacket(worldPosition, name, text));
        }
    }

    public void saveFile(String name, String text) {
        if (!(level instanceof ServerLevel server)) return;
        try {
            files(server).write(name, text, false);
            terminal.write("Saved " + name + "\n");
        } catch (IOException | IllegalArgumentException e) {
            terminal.write("Could not save " + name + ": " + e.getMessage() + "\n");
        }
    }

    // --- Cameras ---

    /** Shows every camera on the network to the people at this terminal, starting with {@code startId}. */
    boolean viewCameras(String startId) {
        if (!(level instanceof ServerLevel server)) return false;
        List<CameraViewPacket.Camera> cameras = new ArrayList<>();
        int start = -1;
        for (ElectricalBlockEntity device : DataNetwork.devices(level, worldPosition)) {
            if (device instanceof SecurityCameraBlockEntity camera) {
                if (camera.deviceId().equals(startId)) start = cameras.size();
                cameras.add(new CameraViewPacket.Camera(camera.deviceId(), camera.label(), camera.getBlockPos(),
                        camera.getBlockState().getValue(WallDeviceBlock.FACING).toYRot(), camera.isPowered()));
            }
        }
        if (start < 0) return false;
        CameraViewPacket packet = new CameraViewPacket(worldPosition, cameras, start);
        for (UUID id : viewers) {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(id);
            if (player != null) ModNetwork.toPlayer(player, packet);
        }
        return true;
    }

    // --- Messages between computers ---

    boolean sendTo(String id, String message) {
        if (DataNetwork.find(level, worldPosition, id) instanceof ComputerBlockEntity other) {
            other.deliver(deviceId(), message);
            return true;
        }
        return false;
    }

    int broadcast(String message) {
        int count = 0;
        for (ElectricalBlockEntity device : DataNetwork.devices(level, worldPosition)) {
            if (device instanceof ComputerBlockEntity other) {
                other.deliver(deviceId(), message);
                count++;
            }
        }
        return count;
    }

    private void deliver(String from, String message) {
        if (runner != null) runner.inbox.add(new String[]{from, message});
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        methods.put("is_on", args -> runner != null);
    }

    // --- Lifecycle ---

    @Override
    public void setRemoved() {
        stopRunner();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        stopRunner();
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("On", on);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        on = tag.getBoolean("On");
    }
}
