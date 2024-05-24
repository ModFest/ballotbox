package net.modfest.ballotbox.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

public record VotingOption(String name, String description, @Nullable String url) {
    public static final Codec<VotingOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(VotingOption::name),
        Codec.STRING.fieldOf("description").forGetter(VotingOption::description),
        Codec.STRING.fieldOf("limit").forGetter(VotingOption::url)
    ).apply(instance, VotingOption::new));
}
