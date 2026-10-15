package net.modfest.ballotbox.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.sounds.Musics;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.packet.OpenVoteScreen;
import oshi.util.tuples.Pair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BallotBoxButtons {
	public static List<Pair<Map<String, ButtonActionType>, Function<Screen, Button.Builder>>> createButtons() {
		var list = new ArrayList<Pair<Map<String, ButtonActionType>, Function<Screen, Button.Builder>>>();

		list.add(new Pair<>(BallotBox.CONFIG.voting_button.value(), (_) -> Button.builder(Component.nullToEmpty("Submission Voting"), _ -> {
			Minecraft.getInstance().setScreenAndShow(new VotingScreen());
			ClientPlayNetworking.send(new OpenVoteScreen());
		}).tooltip(
			!BallotBoxClient.isOpen() ? Tooltip.create(Component.literal("Closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).withStyle(ChatFormatting.GRAY))
				: !BallotBoxClient.isEnabled(Minecraft.getInstance()) ? Tooltip.create(Component.literal("Voting isn't available in singleplayer!").withStyle(ChatFormatting.GRAY))
				: null
		)));

		list.add(new Pair<>(BallotBox.CONFIG.custom_link_button.value(),
			(screen) -> Button.builder(Component.nullToEmpty(BallotBox.CONFIG.custom_link_text.value()), ConfirmLinkScreen.confirmLink(screen, URI.create(BallotBox.CONFIG.custom_link_url.value())))));

		list.add(new Pair<>(BallotBox.CONFIG.credits_button.value(), (screen) -> Button.builder(Component.nullToEmpty(BallotBox.CONFIG.credits_text.value()), b -> {
			Minecraft.getInstance().setScreenAndShow(new WinScreen(false, () -> Minecraft.getInstance().gui.setScreen(screen)));
			Minecraft.getInstance().getMusicManager().stopPlaying();
			Minecraft.getInstance().getMusicManager().startPlaying(Musics.CREDITS);
		})));

		return list;
	}

	public static String match(LayoutElement child, Set<String> buttons) {
		if (child instanceof AbstractWidget widget) {
			if (widget.getMessage().getContents() instanceof TranslatableContents textContent && buttons.contains(textContent.getKey())) {
				return textContent.getKey();
			}

			return buttons.contains(widget.getMessage().getString()) ? widget.getMessage().getString() : null;
		}
		return null;
	}
}
