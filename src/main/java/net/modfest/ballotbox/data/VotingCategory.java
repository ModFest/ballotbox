package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record VotingCategory(String id, String name, String description, String type, int limit, Optional<List<String>> prohibitions) {
	public static final Codec<VotingCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(VotingCategory::id),
		Codec.STRING.fieldOf("name").forGetter(VotingCategory::name),
		Codec.STRING.fieldOf("description").forGetter(VotingCategory::description),
		Codec.STRING.fieldOf("type").forGetter(VotingCategory::type),
		Codec.INT.fieldOf("limit").forGetter(VotingCategory::limit),
		Codec.list(Codec.STRING).optionalFieldOf("prohibitions").forGetter(VotingCategory::prohibitions)
	).apply(instance, VotingCategory::new));
}
