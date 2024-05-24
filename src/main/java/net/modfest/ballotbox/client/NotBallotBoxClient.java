package net.modfest.ballotbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.modfest.ballotbox.BallotBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotBallotBoxClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("%s-client".formatted(BallotBox.ID));

    @Override
    public void onInitializeClient() {
        LOGGER.info("[BallotBox Client] Initialized!");
        BallotBoxClientNetworking.init();
        BallotBoxKeybinds.init();
    }
}