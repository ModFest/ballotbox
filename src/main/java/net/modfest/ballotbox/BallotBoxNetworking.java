package net.modfest.ballotbox;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

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
        BallotBoxPlatformClient.getSelections(player.getUuid()).thenAccept(selections -> {
            ServerPlayNetworking.send(player, new S2CVoteScreenData(BallotBoxPlatformClient.categories, BallotBoxPlatformClient.options, selections));
        });
    }

    private static void handleOpenVoteScreen(OpenVoteScreenPacket packet, ServerPlayNetworking.Context context) {
        sendVoteScreenData(context.player());
    }

    private static void handleUpdateVote(C2SUpdateVote packet, ServerPlayNetworking.Context context) {
        BallotBoxPlatformClient.putSelections(context.player().getUuid(), packet.selections()).thenAccept(success -> {
            if (success) {
                context.player().sendMessage(Text.literal("[BallotBox] ").formatted(Formatting.AQUA).append(Text.literal("Votes Saved! You assigned %s/%s votes over %s/%s categories.".formatted(packet.selections().votes().size(), BallotBoxPlatformClient.categories.stream().mapToInt(VotingCategory::limit).sum(), packet.selections().votes().keySet().size(), BallotBoxPlatformClient.categories.size())).formatted(Formatting.GREEN)), true);
            } else {
                BallotBox.LOGGER.info("zomg posts your secrets in the log {} {} {}", BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value());
            }
        });
    }
}
