package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class BallotBoxConfig extends ReflectiveConfig {
    @Comment("The event ID to use when making requests to the API")
    public final TrackedValue<String> eventId = value("carnival");
    @Comment("The URL to request vote options from, with an optional event ID %s placeholder")
    public final TrackedValue<String> options_url = value("https://platform.modfest.net/event/%s/submissions");
    @Comment("The text to use to replace the bug report button")
    public final TrackedValue<String> bug_text = value("Modfest Discord");
    @Comment("The link to use to replace the bug report button")
    public final TrackedValue<String> bug_url = value("https://discord.gg/gn543Ee");
    @Comment("The number of top results to show when displaying voting results")
    public final TrackedValue<Integer> awardLimit = value(8);
    @Comment("The closing date, as an ISO local date time - or an empty string for none")
    public final TrackedValue<String> closingTime = value("2024-07-28T12:00:00");
}
