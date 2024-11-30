package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class BallotBoxConfig extends ReflectiveConfig {
	@Comment("Whether to replace the send feedback button with a voting button")
	public final TrackedValue<Boolean> replace_feedback = value(true);
	@Comment("Whether to replace the bug report button with another link")
	public final TrackedValue<Boolean> replace_bugs = value(true);
	@Comment("The text to use to replace the bug report button")
	public final TrackedValue<String> bug_text = value("Modfest Discord");
	@Comment("The link to use to replace the bug report button")
	public final TrackedValue<String> bug_url = value("https://discord.gg/gn543Ee");
	@Comment("Whether to replace the realms button on the title screen with a credits button")
	public final TrackedValue<Boolean> replace_realms_credits = value(true);
	@Comment("Whether to replace the player reporting button on the pause screen with a credits button")
	public final TrackedValue<Boolean> replace_reporting_credits = value(true);
	@Comment("The text to use for replacement credits but tons button")
	public final TrackedValue<String> credits_text = value("Modpack Credits");
	@Comment("The number of top results to show when displaying voting results")
	public final TrackedValue<Integer> awardLimit = value(8);
	@Comment("The closing date, as an ISO local date time - or an empty string for none")
	public final TrackedValue<String> closingTime = value("2024-12-16T12:00:00");
}
