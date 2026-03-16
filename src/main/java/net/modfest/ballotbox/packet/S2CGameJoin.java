package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.modfest.ballotbox.BallotBox;

public record S2CGameJoin(String closingTime, int remainingVotes) implements CustomPacketPayload {
	public static final Type<S2CGameJoin> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "game_join"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CGameJoin> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, S2CGameJoin::closingTime,
		ByteBufCodecs.INT, S2CGameJoin::remainingVotes,
		S2CGameJoin::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
