package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class BallotBoxConfig extends ReflectiveConfig {
    @Comment("The event ID to use when making requests to the API")
    public final TrackedValue<String> eventId = value("1.20");
    @Comment("The secret to use when making requests to the API")
    public final TrackedValue<String> platform_secret = value("secret");
    @Comment("The URL to request vote categories from, with an optional event ID %s placeholder")
    public final TrackedValue<String> categories_url = value("https://platform.modfest.net/event/%s");
    @Comment("The URL to request vote options from, with an optional event ID %s placeholder")
    public final TrackedValue<String> options_url = value("https://platform.modfest.net/event/%s/submissions");
    @Comment("The URL to request and post vote selections via, with event ID and player uuid %s placeholders")
    public final TrackedValue<String> selections_url = value("https://platform.modfest.net/event/%s/votes/%s");
    @Comment("The text to use to replace the bug report button")
    public final TrackedValue<String> bug_text = value("Modfest Discord");
    @Comment("The link to use to replace the bug report button")
    public final TrackedValue<String> bug_url = value("https://discord.gg/gn543Ee");
}
