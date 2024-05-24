package net.modfest.ballotbox;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallotBoxClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("%s-client".formatted(BallotBox.ID));

    @Override
    public void onInitializeClient() {
        LOGGER.info("[BallotBox Client] Initialized!");
    }
}