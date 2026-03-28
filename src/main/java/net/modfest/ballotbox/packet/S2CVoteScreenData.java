package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.util.List;

public record S2CVoteScreenData(List<VotingCategory> categories, List<VotingOption> options, VotingSelections selections) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<S2CVoteScreenData> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BallotBox.ID, "vote_screen_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CVoteScreenData> CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodec(VotingCategory.CODEC).apply(ByteBufCodecs.list()), S2CVoteScreenData::categories,
		ByteBufCodecs.fromCodec(VotingOption.CODEC).apply(ByteBufCodecs.list()), S2CVoteScreenData::options,
		ByteBufCodecs.fromCodec(VotingSelections.CODEC), S2CVoteScreenData::selections,
		S2CVoteScreenData::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
