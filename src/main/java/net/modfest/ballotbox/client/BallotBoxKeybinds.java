package net.modfest.ballotbox.client;

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;

public class BallotBoxKeybinds {
    public static final KeyBind OPEN_VOTING_SCREEN = new KeyBind("key.ballotbox.open", InputUtil.Type.KEYSYM, InputUtil.KEY_APOSTROPHE_CODE, "key.ballotbox.category");

    public static void init() {
        KeyBindingHelper.registerKeyBinding(OPEN_VOTING_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(BallotBoxKeybinds::tick);
    }

    private static void tick(MinecraftClient client) {
        while (OPEN_VOTING_SCREEN.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new VotingScreen());
                ClientPlayNetworking.send(new OpenVoteScreenPacket());
            }
        }
    }
}
