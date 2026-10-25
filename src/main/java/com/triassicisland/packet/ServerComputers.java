package com.triassicisland.packet;

import com.triassicisland.computer.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/** Finds the computer a client packet is about, if the sender is allowed to use it. */
final class ServerComputers {
    static void with(ServerPlayer player, BlockPos pos, Consumer<ComputerBlockEntity> action) {
        if (player == null || !player.level().isLoaded(pos)) return;
        if (player.level().getBlockEntity(pos) instanceof ComputerBlockEntity computer && computer.canUse(player)) {
            action.accept(computer);
        }
    }

    private ServerComputers() {}
}
