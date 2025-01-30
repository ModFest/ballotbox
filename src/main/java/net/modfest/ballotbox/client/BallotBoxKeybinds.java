package net.modfest.ballotbox.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.packet.OpenVoteScreen;

public class BallotBoxKeybinds {
	public static final KeyBinding OPEN_VOTING_SCREEN = new KeyBinding("key.ballotbox.open", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_APOSTROPHE, "key.ballotbox.category");

	public static void init() {
		KeyBindingHelper.registerKeyBinding(OPEN_VOTING_SCREEN);
		ClientTickEvents.END_CLIENT_TICK.register(BallotBoxKeybinds::tick);
	}

	private static void tick(MinecraftClient client) {
		while (OPEN_VOTING_SCREEN.wasPressed() && BallotBoxClient.isEnabled(client)) {
			if (!BallotBoxClient.isOpen()) {
				client.inGameHud.setOverlayMessage(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("Voting is unavailable! Voting closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).formatted(Formatting.RED)), false);
            } else if (!BallotBoxClient.hasVotingOptions) {
                client.inGameHud.setOverlayMessage(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("Voting is unavailable! Nothing to vote for.").formatted(Formatting.RED)), false);
            } else if (client.currentScreen == null) {
				client.setScreen(new VotingScreen());
				ClientPlayNetworking.send(new OpenVoteScreen());
			}
		}
	}
}
