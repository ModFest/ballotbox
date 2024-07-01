package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;

import java.util.List;

public record S2CVoteScreenData(List<VotingCategory> categories, List<VotingOption> options, VotingSelections selections) implements CustomPayload {
    public static final CustomPayload.Id<S2CVoteScreenData> ID = new CustomPayload.Id<>(Identifier.of(BallotBox.ID, "vote_screen_data"));
    public static final PacketCodec<RegistryByteBuf, S2CVoteScreenData> CODEC = PacketCodec.tuple(
        PacketCodecs.codec(VotingCategory.CODEC).collect(PacketCodecs.toList()), S2CVoteScreenData::categories,
        PacketCodecs.codec(VotingOption.CODEC).collect(PacketCodecs.toList()), S2CVoteScreenData::options,
        PacketCodecs.codec(VotingSelections.CODEC), S2CVoteScreenData::selections,
        S2CVoteScreenData::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
