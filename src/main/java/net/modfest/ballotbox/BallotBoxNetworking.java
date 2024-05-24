package net.modfest.ballotbox;

import com.google.common.collect.HashMultimap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.util.List;

public class BallotBoxNetworking {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(C2SUpdateVote.ID, C2SUpdateVote.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenVoteScreenPacket.ID, OpenVoteScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenVoteScreenPacket.ID, OpenVoteScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CVoteScreenData.ID, S2CVoteScreenData.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(C2SUpdateVote.ID, BallotBoxNetworking::handleUpdateVote);
        ServerPlayNetworking.registerGlobalReceiver(OpenVoteScreenPacket.ID, BallotBoxNetworking::handleOpenVoteScreen);
    }

    public static void sendVoteScreenData(ServerPlayerEntity player) {
        // TODO: Fetch from API.
        ServerPlayNetworking.send(player, new S2CVoteScreenData(List.of(), List.of(), new VotingSelections(HashMultimap.create())));
    }

    private static void handleOpenVoteScreen(OpenVoteScreenPacket packet, ServerPlayNetworking.Context context) {
        sendVoteScreenData(context.player());
    }

    private static void handleUpdateVote(C2SUpdateVote packet, ServerPlayNetworking.Context context) {
        BallotBox.LOGGER.info("{} updated their votes with {} selections!", context.player().getUuid(), packet.selections().selections().size());
        BallotBox.LOGGER.info("zomg posts your secrets in the log {} {} {}", BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value());
        // TODO: Update via API.
    }
}
