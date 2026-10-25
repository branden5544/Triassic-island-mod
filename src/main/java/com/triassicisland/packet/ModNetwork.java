package com.triassicisland.packet;

import com.triassicisland.TriassicIsland;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            TriassicIsland.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(TerminalOpenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TerminalOpenPacket::encode).decoder(TerminalOpenPacket::decode).consumerMainThread(TerminalOpenPacket::handle).add();
        CHANNEL.messageBuilder(TerminalUpdatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TerminalUpdatePacket::encode).decoder(TerminalUpdatePacket::decode).consumerMainThread(TerminalUpdatePacket::handle).add();
        CHANNEL.messageBuilder(EditorOpenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EditorOpenPacket::encode).decoder(EditorOpenPacket::decode).consumerMainThread(EditorOpenPacket::handle).add();
        CHANNEL.messageBuilder(CameraViewPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CameraViewPacket::encode).decoder(CameraViewPacket::decode).consumerMainThread(CameraViewPacket::handle).add();
        CHANNEL.messageBuilder(PhoneDataPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PhoneDataPacket::encode).decoder(PhoneDataPacket::decode).consumerMainThread(PhoneDataPacket::handle).add();
        CHANNEL.messageBuilder(PhoneActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PhoneActionPacket::encode).decoder(PhoneActionPacket::decode).consumerMainThread(PhoneActionPacket::handle).add();
        CHANNEL.messageBuilder(TerminalInputPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerminalInputPacket::encode).decoder(TerminalInputPacket::decode).consumerMainThread(TerminalInputPacket::handle).add();
        CHANNEL.messageBuilder(TerminalActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerminalActionPacket::encode).decoder(TerminalActionPacket::decode).consumerMainThread(TerminalActionPacket::handle).add();
        CHANNEL.messageBuilder(EditorSavePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EditorSavePacket::encode).decoder(EditorSavePacket::decode).consumerMainThread(EditorSavePacket::handle).add();
    }

    public static void toPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void toServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    private ModNetwork() {}
}
