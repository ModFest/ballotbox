package net.modfest.ballotbox;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.ClassLoaderUtil;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        Gson gson = new Gson();
        List<VotingCategory> categories = gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/categories.json"))), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
        List<VotingOption> options = gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/options.json"))), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
        VotingSelections selections = VotingSelections.CODEC.decode(JsonOps.INSTANCE, gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/selections.json"))), JsonObject.class)).getOrThrow().getFirst();
        ServerPlayNetworking.send(player, new S2CVoteScreenData(categories, options, selections));
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
