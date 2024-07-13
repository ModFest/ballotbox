package net.modfest.ballotbox;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;

import java.util.HashMap;
import java.util.Map;
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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess context, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            CommandManager.literal("vote")
                .executes(c -> execute(c, null, BallotBoxCommands::vote))
        );
        dispatcher.register(
            CommandManager.literal("votes")
                .requires(s -> s.hasPermissionLevel(4))
                .executes(c -> execute(c, null, BallotBoxCommands::votes))
        );
    }

    private static int votes(ServerPlayerEntity player, String ignored, Consumer<Text> feedback) {
        Map<VotingCategory, Multiset<VotingOption>> votes = new HashMap<>();
        BallotBox.STATE.selections().forEach((uuid, selections) -> selections.votes().forEach((category, option) -> {
            if (BallotBoxPlatformClient.categories.containsKey(category) && BallotBoxPlatformClient.options.containsKey(option)) {
                votes.computeIfAbsent(BallotBoxPlatformClient.categories.get(category), k -> HashMultiset.create()).add(BallotBoxPlatformClient.options.get(option));
            }
        }));
        feedback.accept(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("%d players have submitted %d votes so far!".formatted(BallotBox.STATE.selections().size(), votes.size())).formatted(Formatting.AQUA)));
        votes.forEach((category, options) -> {
            feedback.accept(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("--- Top 5 for %s ---".formatted(category.name())).formatted(Formatting.LIGHT_PURPLE)));
            int i = 0;
            for (Multiset.Entry<VotingOption> e : Multisets.copyHighestCountFirst(options).entrySet()) {
                if (i >= BallotBox.CONFIG.category_limit.value()) return;
                feedback.accept(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("%d - %s".formatted(e.getCount(), e.getElement().name())).formatted(Formatting.YELLOW)));
                i++;
            }
        });
        feedback.accept(Text.literal("[BallotBox] ").formatted(Formatting.GREEN).append(Text.literal("--- End Votes ---").formatted(Formatting.AQUA)));
        return votes.size();
    }

    private static int vote(ServerPlayerEntity player, String ignored, Consumer<Text> feedback) {
        ServerPlayNetworking.send(player, new OpenVoteScreenPacket());
        BallotBoxNetworking.sendVoteScreenData(player);
        return 1;
    }
}
