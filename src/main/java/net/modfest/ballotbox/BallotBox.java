package net.modfest.ballotbox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallotBox implements ModInitializer {
    public static final String ID = "ballotbox";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final BallotBoxConfig CONFIG = BallotBoxConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, BallotBoxConfig.class);

    @Override
    public void onInitialize() {
        LOGGER.info("[BallotBox] Initialized!");
        BallotBoxNetworking.init();
        CommandRegistrationCallback.EVENT.register(BallotBoxCommands::register);
    }
}