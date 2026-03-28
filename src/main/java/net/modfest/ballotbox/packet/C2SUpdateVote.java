package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.data.VotingSelections;

public record C2SUpdateVote(VotingSelections selections) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<C2SUpdateVote> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BallotBox.ID, "update_vote"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SUpdateVote> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(VotingSelections.CODEC), C2SUpdateVote::selections, C2SUpdateVote::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
