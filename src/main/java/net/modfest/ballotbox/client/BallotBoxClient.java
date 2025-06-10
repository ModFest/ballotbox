package net.modfest.ballotbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.packet.OpenVoteScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class BallotBoxClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("%s-client".formatted(BallotBox.ID));
	public static Instant closingTime = null;
    public static boolean hasVotingOptions = true;
	public static int remainingVotes = 0;

	public static boolean isEnabled(MinecraftClient client) {
		return !client.isIntegratedServerRunning() && ClientPlayNetworking.canSend(OpenVoteScreen.ID);
	}

	public static boolean isOpen() {
		return closingTime == null || closingTime.isAfter(Instant.now());
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("[BallotBox Client] Initialized!");
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			remainingVotes = 0;
			closingTime = null;
            hasVotingOptions = true;
		});
		BallotBoxClientNetworking.init();
		BallotBoxKeybinds.init();


		var lateModify = Identifier.of(BallotBox.ID, "late");
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, lateModify);
		ScreenEvents.AFTER_INIT.register(lateModify, (client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ApplyModifications applyModifications) applyModifications.ballotbox$applyModifications();
		});
	}
}
