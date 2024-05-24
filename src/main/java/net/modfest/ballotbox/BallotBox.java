package net.modfest.ballotbox;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallotBox implements ModInitializer {
    public static final String ID = "ballotbox";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[BallotBox] Initialized!");
    }
}