package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;

import java.util.List;

public class BallotBoxConfig extends ReflectiveConfig {
	@Comment("Whether to add a voting button")
	public final ButtonSettings voting_button = new ButtonSettings(List.of("menu.feedback", "menu.sendFeedback"), false, true);
	@Comment("Whether to replace the bug report button with another link")
	public final ButtonSettings custom_link_button = new ButtonSettings(List.of("menu.reportBugs"), false, true);
	@Comment("The text to use to replace the bug report button")
	public final TrackedValue<String> custom_link_text = value("ModFest Discord");
	@Comment("The link to use to replace the bug report button")
	public final TrackedValue<String> custom_link_url = value("https://discord.gg/gn543Ee");
	@Comment("Whether to add a credits button")
	public final ButtonSettings credits_button = new ButtonSettings(List.of("menu.online", "menu.playerReporting"), true, true);
	@Comment("The text to use for replacement credits but tons button")
	public final TrackedValue<String> credits_text = value("Modpack Credits");
	@Comment("The number of top results to show when displaying voting results")
	public final TrackedValue<Integer> awardLimit = value(8);
	@Comment("The closing date, as an ISO local date time - or an empty string for none")
	public final TrackedValue<String> closingTime = value("2024-12-16T12:00:00");


	public static class ButtonSettings extends Section {

		public final TrackedValue<ButtonActionType> action_type = value(ButtonActionType.REPLACE);
		public final TrackedValue<ValueList<String>> target_button;

		public final TrackedValue<Boolean> apply_in_main_menu;
		public final TrackedValue<Boolean> apply_in_pause_screen;
		public ButtonSettings() {
			this.target_button = list("");
			this.apply_in_main_menu = value(false);
			this.apply_in_pause_screen = value(false);
		}

		public ButtonSettings(List<String> target, boolean mainMenu, boolean pauseScreen) {
			this.target_button = list("", target.toArray(new String[0]));
			this.apply_in_main_menu = value(mainMenu);
			this.apply_in_pause_screen = value(pauseScreen);
		}
	}
}
