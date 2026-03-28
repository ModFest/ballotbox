package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.modfest.ballotbox.BallotBox;

public record OpenVoteScreen() implements CustomPacketPayload {
	public static final Type<OpenVoteScreen> ID = new Type<>(Identifier.fromNamespaceAndPath(BallotBox.ID, "open_vote_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenVoteScreen> CODEC = StreamCodec.unit(new OpenVoteScreen());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
