package net.modfest.ballotbox.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.BallotBox;

public record S2CGameJoin(String closingTime, int remainingVotes) implements CustomPayload {
    public static final Id<S2CGameJoin> ID = new Id<>(Identifier.of(BallotBox.ID, "game_join"));
    public static final PacketCodec<RegistryByteBuf, S2CGameJoin> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, S2CGameJoin::closingTime,
        PacketCodecs.INTEGER, S2CGameJoin::remainingVotes,
        S2CGameJoin::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
