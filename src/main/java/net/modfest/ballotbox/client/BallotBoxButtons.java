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
import net.minecraft.util.Tuple;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.BallotBoxConfig;
import net.modfest.ballotbox.packet.OpenVoteScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BallotBoxButtons {
	public static List<Tuple<BallotBoxConfig.ButtonSettings, Function<Screen, Button.Builder>>> createButtons() {
		var list = new ArrayList<Tuple<BallotBoxConfig.ButtonSettings, Function<Screen, Button.Builder>>>();

		list.add(new Tuple<>(BallotBox.CONFIG.voting_button, (screen) -> Button.builder(Component.nullToEmpty("Submission Voting"), b -> {
			Minecraft.getInstance().setScreen(new VotingScreen());
			ClientPlayNetworking.send(new OpenVoteScreen());
		}).tooltip(
			!BallotBoxClient.isOpen() ? Tooltip.create(Component.literal("Closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).withStyle(ChatFormatting.GRAY))
				: !BallotBoxClient.isEnabled(Minecraft.getInstance()) ? Tooltip.create(Component.literal("Voting isn't available in singleplayer!").withStyle(ChatFormatting.GRAY))
				: null
		)));

		list.add(new Tuple<>(BallotBox.CONFIG.custom_link_button,
			(screen) -> Button.builder(Component.nullToEmpty(BallotBox.CONFIG.custom_link_text.value()), ConfirmLinkScreen.confirmLink(screen, BallotBox.CONFIG.custom_link_url.value()))));

		list.add(new Tuple<>(BallotBox.CONFIG.credits_button, (screen) -> Button.builder(Component.nullToEmpty(BallotBox.CONFIG.credits_text.value()), b -> {
			Minecraft.getInstance().setScreen(new WinScreen(false, () -> Minecraft.getInstance().setScreen(screen)));
			Minecraft.getInstance().getMusicManager().stopPlaying();
			Minecraft.getInstance().getMusicManager().startPlaying(Musics.CREDITS);
		})));

		return list;
	}

	public static boolean match(LayoutElement child, BallotBoxConfig.ButtonSettings settings) {
		if (child instanceof AbstractWidget widget) {
			if (widget.getMessage().getContents() instanceof TranslatableContents textContent && settings.target_button.value().contains(textContent.getKey())) {
				return true;
			}

			return settings.target_button.value().contains(widget.getMessage().getString());
		}
		return false;
	}
}
