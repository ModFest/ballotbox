package net.modfest.ballotbox.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.sound.MusicType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.BallotBoxConfig;
import net.modfest.ballotbox.packet.OpenVoteScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BallotBoxButtons {
	public static List<Pair<BallotBoxConfig.ButtonSettings, Function<Screen, ButtonWidget.Builder>>> createButtons() {
		var list = new ArrayList<Pair<BallotBoxConfig.ButtonSettings, Function<Screen, ButtonWidget.Builder>>>();

		list.add(new Pair<>(BallotBox.CONFIG.voting_button, (screen) -> ButtonWidget.builder(Text.of("Submission Voting"), b -> {
			MinecraftClient.getInstance().setScreen(new VotingScreen());
			ClientPlayNetworking.send(new OpenVoteScreen());
		}).tooltip(BallotBoxClient.isOpen() ? null : Tooltip.of(Text.literal("Closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).formatted(Formatting.GRAY)))));

		list.add(new Pair<>(BallotBox.CONFIG.custom_link_button,
			(screen) -> ButtonWidget.builder(Text.of(BallotBox.CONFIG.custom_link_text.value()), ConfirmLinkScreen.opening(screen, BallotBox.CONFIG.custom_link_url.value()))));

		list.add(new Pair<>(BallotBox.CONFIG.credits_button, (screen) -> ButtonWidget.builder(Text.of(BallotBox.CONFIG.credits_text.value()), b -> {
			MinecraftClient.getInstance().setScreen(new CreditsScreen(false, () -> MinecraftClient.getInstance().setScreen(screen)));
			MinecraftClient.getInstance().getMusicTracker().stop();
			MinecraftClient.getInstance().getMusicTracker().play(new MusicInstance(MusicType.CREDITS));
		})));

		return list;
	}

	public static boolean match(Widget child, BallotBoxConfig.ButtonSettings settings) {
		if (child instanceof ClickableWidget widget) {
			if (widget.getMessage().getContent() instanceof TranslatableTextContent textContent && settings.target_button.value().contains(textContent.getKey())) {
				return true;
			}

			return settings.target_button.value().contains(widget.getMessage().getString());
		}
		return false;
	}
}
