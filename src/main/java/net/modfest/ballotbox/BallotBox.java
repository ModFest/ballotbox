package net.modfest.ballotbox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.packet.S2CGameJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class BallotBox implements ModInitializer {
	public static final String ID = "ballotbox";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final BallotBoxConfig CONFIG = BallotBoxConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, BallotBoxConfig.class);
	public static final String STATE_KEY = "ballotbox_ballots";
	public static BallotState STATE = null;
	public static Instant closingTime = null;

	public static String relativeTime(Instant then) {
		Instant now = Instant.now();
		long offset = now.toEpochMilli() - then.toEpochMilli();
		long days = TimeUnit.MILLISECONDS.toDays(Math.abs(offset));
		if (days > 0) return (offset > 0 ? "%s days ago" : "in %s days").formatted(days);
		long hours = TimeUnit.MILLISECONDS.toHours(Math.abs(offset));
		if (hours > 0) return (offset > 0 ? "%s hours ago" : "in %s hours").formatted(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(offset));
		if (minutes > 0) return (offset > 0 ? "%s minutes ago" : "in %s minutes").formatted(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(offset);
		return (offset > 0 ? "%s seconds ago" : "in %s seconds").formatted(seconds);
	}

	public static boolean isEnabled(MinecraftServer server) {
		return !server.isSingleplayer();
	}

	public static boolean isOpen() {
		return closingTime == null || closingTime.isAfter(Instant.now());
	}

	public static Instant parseClosingTime(String value) {
		try {
			if (!value.isEmpty()) return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
		} catch (DateTimeException e) {
			LOGGER.error("Failed to parse configured closing time '{}', ignoring...", value, e);
		}
		return null;
	}

	@Override
	public void onInitialize() {
		closingTime = parseClosingTime(CONFIG.closingTime.value());
		BallotBoxNetworking.init();
		CommandRegistrationCallback.EVENT.register(BallotBoxCommands::register);
		ServerWorldEvents.LOAD.register(((server, world) -> {
			if (world.getRegistryKey() == World.OVERWORLD) {
				STATE = world.getPersistentStateManager().getOrCreate(BallotState.getPersistentStateType(), STATE_KEY);
			}
		}));
		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			if (!isEnabled(server)) return;
			BallotBoxPlatformClient.init(server.getResourceManager());
		}));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
			if (!isEnabled(server)) return;
			BallotBoxPlatformClient.init(resourceManager);
		}));
		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
			if (!ServerPlayNetworking.canSend(handler.getPlayer(), S2CGameJoin.ID)) return;
			VotingSelections selections = STATE.selections().get(handler.getPlayer().getUuid());
			int totalVotes = BallotBoxPlatformClient.categories.values().stream().mapToInt(VotingCategory::limit).sum();
			int remainingVotes = totalVotes - (selections == null ? 0 : selections.votes().size());
			sender.sendPacket(new S2CGameJoin(CONFIG.closingTime.value(), remainingVotes));
		}));
		LOGGER.info("[BallotBox] Initialized!");
	}
}