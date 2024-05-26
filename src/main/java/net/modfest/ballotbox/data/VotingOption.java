package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VotingOption(String id, String name, String description, String type) {
    public static final Codec<VotingOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(VotingOption::id),
        Codec.STRING.fieldOf("name").forGetter(VotingOption::name),
        Codec.STRING.fieldOf("description").forGetter(VotingOption::description),
        Codec.STRING.fieldOf("type").forGetter(VotingOption::type)
    ).apply(instance, VotingOption::new));
}
