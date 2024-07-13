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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BallotBoxPlatformClient {
    public static final Identifier CATEGORIES_DATA = Identifier.of(BallotBox.ID, "ballot/categories.json");
    public final static Gson GSON = new Gson();
    public static Map<String, VotingOption> options = new HashMap<>();
    public static Map<String, VotingCategory> categories = new HashMap<>();

    public static void init(ResourceManager resourceManager) {
        if (options.isEmpty()) options = getOptions(BallotBox.CONFIG.eventId.value());
        try {
            categories.clear();
            GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(CATEGORIES_DATA).getInputStream())), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).forEach(category -> categories.put(category.id(), category));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, VotingOption> getOptions(String eventId) {
        String uri = BallotBox.CONFIG.options_url.value().formatted(eventId);
        BallotBox.LOGGER.info("[BallotBox] Retrieving vote options from %s!".formatted(uri));
        Map<String, VotingOption> options = new HashMap<>();
        try {
            GSON.fromJson(new BufferedReader(new InputStreamReader((new URI(uri)).toURL().openStream())), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).forEach(option -> options.put(option.id(), option));
        } catch (IOException | URISyntaxException e) {
            BallotBox.LOGGER.error("[BallotBox] Failed to retrieve ballotbox options from specified url", e);
        }
        return options;
    }

    public static CompletableFuture<VotingSelections> getSelections(UUID playerId) {
        return CompletableFuture.completedFuture(BallotBox.STATE.selections().getOrDefault(playerId, new VotingSelections(HashMultimap.create())));
    }

    public static CompletableFuture<Boolean> putSelections(UUID uuid, VotingSelections playerSelections) {
        BallotBox.STATE.selections().put(uuid, playerSelections);
        BallotBox.STATE.markDirty();
        return CompletableFuture.completedFuture(true);
    }
}
