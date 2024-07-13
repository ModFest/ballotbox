package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.data.VotingSelections;

public record C2SUpdateVote(VotingSelections selections) implements CustomPayload {
    public static final CustomPayload.Id<C2SUpdateVote> ID = new CustomPayload.Id<>(Identifier.of(BallotBox.ID, "update_vote"));
    public static final PacketCodec<RegistryByteBuf, C2SUpdateVote> CODEC = PacketCodec.tuple(
        PacketCodecs.codec(VotingSelections.CODEC), C2SUpdateVote::selections, C2SUpdateVote::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
