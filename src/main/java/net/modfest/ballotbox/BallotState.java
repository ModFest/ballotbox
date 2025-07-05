package net.modfest.ballotbox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.modfest.ballotbox.data.VotingSelections;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BallotState extends PersistentState {
	private static final String STATE_KEY = "ballotbox_ballots";

	public static final Codec<BallotState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Uuids.CODEC, VotingSelections.CODEC).xmap(s -> (Map<UUID, VotingSelections>) new ConcurrentHashMap<>(s), ConcurrentHashMap::new).fieldOf("selections").forGetter(BallotState::selections)
	).apply(instance, BallotState::new));

	public static final PersistentStateType<BallotState> TYPE = new PersistentStateType<>(STATE_KEY,
		() -> new BallotState(new ConcurrentHashMap<>()),
		BallotState.CODEC,
		null);

	private final Map<UUID, VotingSelections> selections;

	private BallotState(Map<UUID, VotingSelections> selections) {
		this.selections = selections;
	}

	public Map<UUID, VotingSelections> selections() {
		return selections;
	}
}
