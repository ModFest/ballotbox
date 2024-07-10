package net.modfest.ballotbox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallotBox implements ModInitializer {
    public static final String ID = "ballotbox";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final BallotBoxConfig CONFIG = BallotBoxConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, BallotBoxConfig.class);
    public static final String STATE_KEY = "ballotbox_ballots";
    public static BallotState STATE = null;


    @Override
    public void onInitialize() {
        LOGGER.info("[BallotBox] Initialized!");
        BallotBoxNetworking.init();
        CommandRegistrationCallback.EVENT.register(BallotBoxCommands::register);
        ServerWorldEvents.LOAD.register(((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                STATE = world.getPersistentStateManager().getOrCreate(BallotState.getPersistentStateType(), STATE_KEY);
            }
        }));
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            if (server.isSingleplayer()) return;
            BallotBoxPlatformClient.init(server.getResourceManager());
        }));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
            if (server.isSingleplayer()) return;
            BallotBoxPlatformClient.init(resourceManager);
        }));
    }
}