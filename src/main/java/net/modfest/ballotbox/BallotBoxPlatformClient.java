package net.modfest.ballotbox;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BallotBoxPlatformClient {
    public static final Identifier CATEGORIES_DATA = Identifier.of(BallotBox.ID, "ballot/categories.json");
    public final static Gson GSON = new Gson();
    public static HttpClient client = HttpClient.newBuilder().build();
    public static List<VotingOption> options = new ArrayList<>();
    public static List<VotingCategory> categories = new ArrayList<>();

    public static void init(ResourceManager resourceManager) {
        if (options.isEmpty()) options = getOptions(BallotBox.CONFIG.eventId.value());
        try {
            categories = GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(CATEGORIES_DATA).getInputStream())), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<VotingOption> getOptions(String eventId) {
        String uri = BallotBox.CONFIG.options_url.value().formatted(BallotBox.CONFIG.eventId.value());
        BallotBox.LOGGER.info("[BallotBox] Retrieving vote options from %s!".formatted(uri));
        try {
            return GSON.fromJson(new BufferedReader(new InputStreamReader((new URI(uri)).toURL().openStream())), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).toList();
        } catch (IOException | URISyntaxException e) {
            BallotBox.LOGGER.error("Failed to retrieve ballotbox options from specified url", e);
            return new ArrayList<>();
        }
    }

    public static CompletableFuture<VotingSelections> getSelections(UUID playerId) {
        String uri = BallotBox.CONFIG.selections_url.value().formatted(BallotBox.CONFIG.eventId.value(), playerId.toString());
        // BallotBox.LOGGER.info("[BallotBox] I'm totally getting selections from %s!".formatted(uri));
        return CompletableFuture.supplyAsync(() -> {
            try { // Simulate request lag
                Thread.sleep(5);
            } catch (Exception ignored2) {
            }
            return BallotBox.STATE.selections().getOrDefault(playerId, new VotingSelections(HashMultimap.create()));
        });
    }

    public static CompletableFuture<Boolean> putSelections(UUID uuid, VotingSelections playerSelections) {
        String uri = BallotBox.CONFIG.options_url.value().formatted(BallotBox.CONFIG.eventId.value());
        // BallotBox.LOGGER.info("[BallotBox] I'm totally posting selections to %s!".formatted(uri));
        return CompletableFuture.supplyAsync(() -> {
            try { // Simulate request lag
                Thread.sleep(5);
            } catch (Exception ignored2) {
            }
            BallotBox.STATE.selections().put(uuid, playerSelections);
            BallotBox.STATE.markDirty();
            return true;
        });
    }
}
