package net.modfest.ballotbox;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class BallotBoxConfig extends ReflectiveConfig {
    @Comment("The secret to use when making requests to the ModFest platform API")
    public final TrackedValue<String> platform_secret = value("secret");
}
