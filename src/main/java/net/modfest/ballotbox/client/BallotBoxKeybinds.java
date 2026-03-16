package net.modfest.ballotbox.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.packet.OpenVoteScreen;

public class BallotBoxKeybinds {
	public static final KeyMapping OPEN_VOTING_SCREEN = new KeyMapping("key.ballotbox.open", InputConstants.Type.KEYSYM, InputConstants.KEY_APOSTROPHE, "key.ballotbox.category");

	public static void init() {
		KeyBindingHelper.registerKeyBinding(OPEN_VOTING_SCREEN);
		ClientTickEvents.END_CLIENT_TICK.register(BallotBoxKeybinds::tick);
	}

	private static void tick(Minecraft client) {
		while (OPEN_VOTING_SCREEN.consumeClick() && BallotBoxClient.isEnabled(client)) {
			if (!BallotBoxClient.isOpen()) {
				client.gui.setOverlayMessage(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("Voting is unavailable! Voting closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).withStyle(ChatFormatting.RED)), false);
			} else if (client.screen == null) {
				client.setScreen(new VotingScreen());
				ClientPlayNetworking.send(new OpenVoteScreen());
			}
		}
	}
}
