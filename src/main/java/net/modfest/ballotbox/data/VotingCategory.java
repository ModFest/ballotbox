package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VotingCategory(String name, String description, int limit, List<String> prohibitons) {
    public static final Codec<VotingCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(VotingCategory::name),
        Codec.STRING.fieldOf("description").forGetter(VotingCategory::description),
        Codec.INT.fieldOf("limit").forGetter(VotingCategory::limit),
        Codec.list(Codec.STRING).fieldOf("prohibitons").forGetter(VotingCategory::prohibitons)
    ).apply(instance, VotingCategory::new));
}
