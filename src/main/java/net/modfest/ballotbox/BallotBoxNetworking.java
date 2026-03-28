package net.modfest.ballotbox;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.packet.C2SUpdateVote;
import net.modfest.ballotbox.packet.OpenVoteScreen;
import net.modfest.ballotbox.packet.S2CGameJoin;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.util.ArrayList;

public class BallotBoxNetworking {
	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(C2SUpdateVote.ID, C2SUpdateVote.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(OpenVoteScreen.ID, OpenVoteScreen.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CGameJoin.ID, S2CGameJoin.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(OpenVoteScreen.ID, OpenVoteScreen.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CVoteScreenData.ID, S2CVoteScreenData.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(C2SUpdateVote.ID, BallotBoxNetworking::handleUpdateVote);
		ServerPlayNetworking.registerGlobalReceiver(OpenVoteScreen.ID, BallotBoxNetworking::handleOpenVoteScreen);
	}

	public static void sendVoteScreenData(ServerPlayer player) {
		if (!BallotBox.isOpen()) return;
		BallotBoxPlatformClient.getSelections(player.getUUID()).thenAccept(selections -> ServerPlayNetworking.send(player, new S2CVoteScreenData(new ArrayList<>(BallotBoxPlatformClient.categories.values()), new ArrayList<>(BallotBoxPlatformClient.options.values()), selections)));
	}

	private static void handleOpenVoteScreen(OpenVoteScreen packet, ServerPlayNetworking.Context context) {
		sendVoteScreenData(context.player());
	}

	private static void handleUpdateVote(C2SUpdateVote packet, ServerPlayNetworking.Context context) {
		if (!BallotBox.isOpen()) return;
		BallotBoxPlatformClient.putSelections(context.player().getUUID(), packet.selections()).thenAccept(success -> {
			if (success) {
				context.player().sendOverlayMessage(Component.literal("[BallotBox] ").withStyle(ChatFormatting.AQUA).append(Component.literal("Votes Saved! You assigned %s/%s votes over %s/%s categories.".formatted(packet.selections().votes().size(), BallotBoxPlatformClient.categories.values().stream().mapToInt(VotingCategory::limit).sum(), packet.selections().votes().keySet().size(), BallotBoxPlatformClient.categories.size())).withStyle(ChatFormatting.GREEN)));
			} else {
				BallotBox.LOGGER.info("[BallotBox] Failed to save selections from player {}!", context.player().getName());
			}
		});
	}
}
