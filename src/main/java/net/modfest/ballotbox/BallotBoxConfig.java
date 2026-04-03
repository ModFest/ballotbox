package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

import java.util.Map;

public class BallotBoxConfig extends ReflectiveConfig {
	@Comment("Where to add a voting button, anchored to existing button translation keys")
	public final TrackedValue<Map<String, ButtonActionType>> voting_button = value(ValueMap.builder(ButtonActionType.REPLACE)
		.put("menu.feedback", ButtonActionType.REPLACE)
		.put("menu.sendFeedback", ButtonActionType.REPLACE)
		.build());
	@Comment("Where to add a custom link button, anchored to existing button translation keys")
	public final TrackedValue<Map<String, ButtonActionType>> custom_link_button = value(ValueMap.builder(ButtonActionType.REPLACE)
		.put("menu.reportBugs", ButtonActionType.REPLACE)
		.build());
	@Comment("The text to use to replace the bug report button")
	public final TrackedValue<String> custom_link_text = value("ModFest Discord");
	@Comment("The link to use to replace the bug report button")
	public final TrackedValue<String> custom_link_url = value("https://discord.gg/gn543Ee");
	@Comment("Where to add a credits button, anchored to existing button translation keys")
	public final TrackedValue<Map<String, ButtonActionType>> credits_button = value(ValueMap.builder(ButtonActionType.REPLACE)
		.put("menu.multiplayer", ButtonActionType.INSERT_AFTER)
		.put("menu.playerReporting", ButtonActionType.REPLACE)
		.build());
	@Comment("The text to use for replacement credits but tons button")
	public final TrackedValue<String> credits_text = value("Modpack Credits");
	@Comment("The number of top results to show when displaying voting results")
	public final TrackedValue<Integer> awardLimit = value(8);
	@Comment("The closing date, as an ISO local date time - or an empty string for none")
	public final TrackedValue<String> closingTime = value("2024-12-16T12:00:00");
	@Comment("Settings for the reminder on the pause screen")
	public final ReminderSettings reminder_settings = new ReminderSettings();

	public static class ReminderSettings extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section {
		public final TrackedValue<Integer> reminder_x_offset;
		public final TrackedValue<Integer> reminder_y_offset;

		public ReminderSettings() {
			this.reminder_x_offset = value(0);
			this.reminder_y_offset = value(0);
		}
	}
}
