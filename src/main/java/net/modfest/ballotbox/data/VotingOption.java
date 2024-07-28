package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VotingOption(String id, String name, String description, Platform platform) {
	public static final Codec<VotingOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(VotingOption::id),
		Codec.STRING.fieldOf("name").forGetter(VotingOption::name),
		Codec.STRING.fieldOf("description").forGetter(VotingOption::description),
		Platform.CODEC.fieldOf("platform").forGetter(VotingOption::platform)
	).apply(instance, VotingOption::new));

	public record Platform(String type, String project_id, String version_id) {
		public static final Codec<Platform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(Platform::type),
			Codec.STRING.fieldOf("project_id").forGetter(Platform::project_id),
			Codec.STRING.fieldOf("version_id").forGetter(Platform::version_id)
		).apply(instance, Platform::new));
	}
}
