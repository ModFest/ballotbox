package net.modfest.ballotbox.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.packet.OpenVoteScreen;
import net.modfest.ballotbox.packet.S2CGameJoin;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

public class BallotBoxClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(S2CGameJoin.ID, BallotBoxClientNetworking::handleGameJoin);
        ClientPlayNetworking.registerGlobalReceiver(S2CVoteScreenData.ID, BallotBoxClientNetworking::handleVoteScreenData);
        ClientPlayNetworking.registerGlobalReceiver(OpenVoteScreen.ID, BallotBoxClientNetworking::handleOpenVoteScreen);
    }

    private static void handleGameJoin(S2CGameJoin packet, ClientPlayNetworking.Context context) {
        NotBallotBoxClient.closingTime = BallotBox.parseClosingTime(packet.closingTime());
        NotBallotBoxClient.remainingVotes = packet.remainingVotes();
    }

    private static void handleOpenVoteScreen(OpenVoteScreen packet, ClientPlayNetworking.Context context) {
        MinecraftClient.getInstance().setScreen(new VotingScreen());
    }

    private static void handleVoteScreenData(S2CVoteScreenData packet, ClientPlayNetworking.Context context) {
        if (MinecraftClient.getInstance().currentScreen instanceof VotingScreen vs) {
            vs.load(packet);
        }
    }
}
