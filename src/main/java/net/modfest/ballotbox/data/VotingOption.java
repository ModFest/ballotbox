package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record VotingOption(String id, Optional<String> mod_id, String name, String description, Platform platform) {
	public static final Codec<VotingOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(VotingOption::id),
		Codec.STRING.optionalFieldOf("mod_id").forGetter(VotingOption::mod_id),
		Codec.STRING.fieldOf("name").forGetter(VotingOption::name),
		Codec.STRING.fieldOf("description").forGetter(VotingOption::description),
		Platform.CODEC.fieldOf("platform").forGetter(VotingOption::platform)
	).apply(instance, VotingOption::new));

	public record Platform(String type, Optional<String> project_id, Optional<String> homepage_url) {
		public static final Codec<Platform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(Platform::type),
			Codec.STRING.optionalFieldOf("project_id").forGetter(Platform::project_id),
			Codec.STRING.optionalFieldOf("homepage_url").forGetter(Platform::homepage_url)
		).apply(instance, Platform::new));
	}
}
