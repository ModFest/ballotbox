package net.modfest.ballotbox;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;

import java.util.function.Consumer;

public class BallotBoxCommands {
    public interface BallotBoxCommandExecutor {
        int execute(ServerPlayerEntity player, String arg, Consumer<Text> feedback);
    }

    public static int execute(CommandContext<ServerCommandSource> context, String arg, BallotBoxCommandExecutor executor) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            BallotBox.LOGGER.error("[BallotBox] Commands cannot be invoked by a non-player");
            return 0;
        }

        try {
            return executor.execute(player, arg != null ? context.getArgument(arg, String.class) : null, t -> context.getSource().sendFeedback(() -> t, false));
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> Text.literal("Command failed! Check log for details.").formatted(Formatting.RED), false);
            BallotBox.LOGGER.error("[BallotBox] Error while executing command: {}", context.getInput(), e);
            return 0;
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext context, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            CommandManager.literal("vote")
                .executes(c -> execute(c, null, BallotBoxCommands::vote))
        );
    }

    private static int vote(ServerPlayerEntity player, String ignored, Consumer<Text> feedback) {
        ServerPlayNetworking.send(player, new OpenVoteScreenPacket());
        BallotBoxNetworking.sendVoteScreenData(player);
        return 1;
    }
}
