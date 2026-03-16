package net.modfest.ballotbox;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BallotBoxPlatformClient {
	public static final ResourceLocation CATEGORIES_DATA = ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "ballot/categories.json");
	public static final ResourceLocation OPTIONS_DATA = ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "ballot/options.json");
	public final static Gson GSON = new Gson();
	public static Map<String, VotingOption> options = new ConcurrentHashMap<>();
	public static Map<String, VotingCategory> categories = new ConcurrentHashMap<>();

	public static void init(ResourceManager resourceManager) {
		try {
			categories.clear();
			options.clear();
			GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(CATEGORIES_DATA).open())), JsonArray.class).asList().stream().map(e -> VotingCategory.CODEC.decode(JsonOps.INSTANCE, e).mapOrElse(Pair::getFirst, a -> null)).filter(Objects::nonNull).forEach(category -> categories.put(category.id(), category));
			GSON.fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResourceOrThrow(OPTIONS_DATA).open())), JsonArray.class).asList().stream().map(e -> VotingOption.CODEC.decode(JsonOps.INSTANCE, e).mapOrElse(Pair::getFirst, a -> null)).filter(Objects::nonNull).forEach(option -> options.put(option.id(), option));
		} catch (Exception e) {
			BallotBox.LOGGER.info("[BallotBox] Failed to load ballotbox data!", e);
		}
	}

	public static CompletableFuture<VotingSelections> getSelections(UUID playerId) {
		return CompletableFuture.completedFuture(BallotBox.STATE.selections().getOrDefault(playerId, new VotingSelections(HashMultimap.create())));
	}

	public static CompletableFuture<Boolean> putSelections(UUID uuid, VotingSelections playerSelections) {
		BallotBox.STATE.selections().put(uuid, playerSelections);
		BallotBox.STATE.setDirty();
		return CompletableFuture.completedFuture(true);
	}
}
