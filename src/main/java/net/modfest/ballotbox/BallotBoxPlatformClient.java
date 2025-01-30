package net.modfest.ballotbox;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BallotBoxPlatformClient {
	public static final Identifier CATEGORIES_DATA = Identifier.of(BallotBox.ID, "ballot/categories.json");
	public static final Identifier OPTIONS_DATA = Identifier.of(BallotBox.ID, "ballot/options.json");
	public final static Gson GSON = new Gson();
	public static Map<String, VotingOption> options = new ConcurrentHashMap<>();
	public static Map<String, VotingCategory> categories = new ConcurrentHashMap<>();

	public static void init(ResourceManager resourceManager) {
		try {
			categories.clear();
            Optional<Resource> categoriesData = resourceManager.getResource(CATEGORIES_DATA);
            if (categoriesData.isPresent()) {
                GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(CATEGORIES_DATA).getInputStream())), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).forEach(category -> categories.put(category.id(), category));
            }
			options.clear();
            Optional<Resource> optionsData = resourceManager.getResource(OPTIONS_DATA);
            if (optionsData.isPresent()) {
                GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(OPTIONS_DATA).getInputStream())), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).getOrThrow().getFirst()).forEach(option -> options.put(option.id(), option));
            }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
