package net.modfest.ballotbox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.modfest.ballotbox.data.VotingSelections;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BallotState extends PersistentState {
	public static final Codec<BallotState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Uuids.CODEC, VotingSelections.CODEC).xmap(s -> (Map<UUID, VotingSelections>) new ConcurrentHashMap<>(s), ConcurrentHashMap::new).fieldOf("selections").forGetter(BallotState::selections)
	).apply(instance, BallotState::new));

	public static PersistentState.Type<BallotState> getPersistentStateType() {
		return new PersistentState.Type<>(() -> new BallotState(new ConcurrentHashMap<>()), BallotState::fromNbt, null);
	}

	private final Map<UUID, VotingSelections> selections;

	private BallotState(Map<UUID, VotingSelections> selections) {
		this.selections = selections;
	}

	private static BallotState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		return BallotState.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("data")).getOrThrow().getFirst();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.put("data", BallotState.CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
		return nbt;
	}

	public Map<UUID, VotingSelections> selections() {
		return selections;
	}
}
