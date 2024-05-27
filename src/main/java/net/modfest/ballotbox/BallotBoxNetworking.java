package net.modfest.ballotbox;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.ClassLoaderUtil;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BallotBoxNetworking {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(C2SUpdateVote.ID, C2SUpdateVote.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenVoteScreenPacket.ID, OpenVoteScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenVoteScreenPacket.ID, OpenVoteScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CVoteScreenData.ID, S2CVoteScreenData.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(C2SUpdateVote.ID, BallotBoxNetworking::handleUpdateVote);
        ServerPlayNetworking.registerGlobalReceiver(OpenVoteScreenPacket.ID, BallotBoxNetworking::handleOpenVoteScreen);
    }

    private final static Gson gson = new Gson();
    private final static Map<UUID, VotingSelections> selections = new HashMap<>();
    private static List<VotingCategory> categories = null;
    private static List<VotingOption> options = null;

    public static void fetchData() {
        if (categories == null) categories = gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/categories.json"))), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
        if (options == null) options = gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/options.json"))), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
    }

    public static void sendVoteScreenData(ServerPlayerEntity player) {
        // TODO: Fetch from API.
        fetchData();
        selections.computeIfAbsent(player.getUuid(), uuid -> VotingSelections.CODEC.decode(JsonOps.INSTANCE, gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/selections.json"))), JsonObject.class)).getOrThrow().getFirst());
        ServerPlayNetworking.send(player, new S2CVoteScreenData(categories, options, selections.get(player.getUuid())));
    }

    private static void handleOpenVoteScreen(OpenVoteScreenPacket packet, ServerPlayNetworking.Context context) {
        sendVoteScreenData(context.player());
    }

    private static void handleUpdateVote(C2SUpdateVote packet, ServerPlayNetworking.Context context) {
        // TODO: Update via API. API should validate stuff like invalid selections - only continue on success.
        selections.put(context.player().getUuid(), packet.selections());
        fetchData();
        context.player().sendMessage(Text.literal("[BallotBox] ").formatted(Formatting.AQUA).append(Text.literal("Votes Saved! You assigned %s/%s votes over %s/%s categories.".formatted(packet.selections().selections().size(), categories.stream().mapToInt(VotingCategory::limit).sum(), packet.selections().selections().keySet().size(), categories.size())).formatted(Formatting.GREEN)), true);
        BallotBox.LOGGER.info("zomg posts your secrets in the log {} {} {}", BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value(), BallotBox.CONFIG.platform_secret.value());
    }
}
