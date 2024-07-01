package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.BallotBox;

public record OpenVoteScreenPacket() implements CustomPayload {
    public static final Id<OpenVoteScreenPacket> ID = new Id<>(Identifier.of(BallotBox.ID, "open_vote_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenVoteScreenPacket> CODEC = PacketCodec.unit(new OpenVoteScreenPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
