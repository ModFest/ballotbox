package net.modfest.ballotbox.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

public class BallotBoxClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(S2CVoteScreenData.ID, BallotBoxClientNetworking::handleVoteScreenData);
        ClientPlayNetworking.registerGlobalReceiver(OpenVoteScreenPacket.ID, BallotBoxClientNetworking::handleOpenVoteScreen);
    }

    private static void handleOpenVoteScreen(OpenVoteScreenPacket packet, ClientPlayNetworking.Context context) {
        MinecraftClient.getInstance().setScreen(new VotingScreen());
    }

    private static void handleVoteScreenData(S2CVoteScreenData packet, ClientPlayNetworking.Context context) {
        if (MinecraftClient.getInstance().currentScreen instanceof VotingScreen vs) {
            vs.load(packet);
        }
    }
}
