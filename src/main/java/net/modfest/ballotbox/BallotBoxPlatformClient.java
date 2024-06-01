package net.modfest.ballotbox;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.ClassLoaderUtil;
import com.mojang.serialization.JsonOps;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BallotBoxPlatformClient {
    public static HttpClient client = HttpClient.newBuilder().build();
    private final static Gson gson = new Gson();
    private final static Map<UUID, VotingSelections> selections = new HashMap<>();
    public static List<VotingCategory> categories = null;
    public static List<VotingOption> options = null;

    public static void init() {
        if (categories == null) categories = getCategories(BallotBox.CONFIG.eventId.value());
        if (options == null) options = getOptions(BallotBox.CONFIG.eventId.value());
    }

    private static List<VotingCategory> getCategories(String eventId) {
        String uri = BallotBox.CONFIG.categories_url.value().formatted(BallotBox.CONFIG.eventId.value());
        BallotBox.LOGGER.info("[BallotBox] I'm totally getting categories from %s!".formatted(uri));
        return gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/categories.json"))), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
    }

    private static List<VotingOption> getOptions(String eventId) {
        String uri = BallotBox.CONFIG.options_url.value().formatted(BallotBox.CONFIG.eventId.value());
        BallotBox.LOGGER.info("[BallotBox] I'm totally getting options from %s!".formatted(uri));
        return gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/options.json"))), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
    }

    public static VotingSelections getSelections(UUID playerId) {
        try { // Simulate request lag
            Thread.sleep(500);
        } catch (Exception ignored2) {
        }
        String uri = BallotBox.CONFIG.selections_url.value().formatted(BallotBox.CONFIG.eventId.value(), playerId.toString());
        BallotBox.LOGGER.info("[BallotBox] I'm totally getting selections from %s!".formatted(uri));
        return selections.computeIfAbsent(playerId, BallotBoxPlatformClient::getSelectionsInternal);
    }

    private static VotingSelections getSelectionsInternal(UUID playerId) {
        return VotingSelections.CODEC.decode(JsonOps.INSTANCE, gson.fromJson(new BufferedReader(new InputStreamReader(ClassLoaderUtil.getClassLoader(BallotBoxNetworking.class).getResourceAsStream("test/selections.json"))), JsonObject.class)).getOrThrow().getFirst();
    }

    public static boolean putSelections(UUID uuid, VotingSelections playerSelections) {
        String uri = BallotBox.CONFIG.options_url.value().formatted(BallotBox.CONFIG.eventId.value());
        BallotBox.LOGGER.info("[BallotBox] I'm totally posting selections to %s!".formatted(uri));
        selections.put(uuid, playerSelections);
        return true;
    }
}
