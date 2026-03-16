package net.modfest.ballotbox;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.packet.OpenVoteScreen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BallotBoxCommands {
	public interface BallotBoxCommandExecutor {
		int execute(ServerPlayer player, String arg, Consumer<Component> feedback);
	}

	public static int execute(CommandContext<CommandSourceStack> context, String arg, BallotBoxCommandExecutor executor) {
		ServerPlayer player = context.getSource().getPlayer();
		try {
			return executor.execute(player, arg != null ? context.getArgument(arg, String.class) : null, t -> context.getSource().sendSuccess(() -> t, false));
		} catch (Exception e) {
			context.getSource().sendSuccess(() -> Component.literal("Command failed! Check log for details.").withStyle(ChatFormatting.RED), false);
			BallotBox.LOGGER.error("[BallotBox] Error while executing command: {}", context.getInput(), e);
			return 0;
		}
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
		dispatcher.register(
			Commands.literal("vote")
				.executes(c -> execute(c, null, (p, a1, f) -> BallotBoxCommands.vote(p, f)))
		);
		dispatcher.register(
			Commands.literal("votes")
				.requires(s -> s.hasPermission(4))
				.executes(c -> execute(c, null, (p, a1, f) -> BallotBoxCommands.votes(f)))
		);
	}

	private static int votes(Consumer<Component> feedback) {
		Map<VotingCategory, Multiset<VotingOption>> votes = new ConcurrentHashMap<>();
		BallotBox.STATE.selections().forEach((uuid, selections) -> selections.votes().forEach((category, option) -> {
			if (BallotBoxPlatformClient.categories.containsKey(category) && BallotBoxPlatformClient.options.containsKey(option)) {
				votes.computeIfAbsent(BallotBoxPlatformClient.categories.get(category), k -> HashMultiset.create()).add(BallotBoxPlatformClient.options.get(option));
			}
		}));
		if (BallotBox.closingTime != null) feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal((BallotBox.isOpen() ? "Voting closes %s." : "Voting closed %s.").formatted(BallotBox.relativeTime(BallotBox.closingTime))).withStyle(ChatFormatting.AQUA)));
		feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("%d players have submitted %d votes!".formatted(BallotBox.STATE.selections().size(), votes.values().stream().mapToInt(Multiset::size).sum())).withStyle(ChatFormatting.AQUA)));
		votes.forEach((category, options) -> {
			feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("--- Top %d for %s ---".formatted(BallotBox.CONFIG.awardLimit.value(), category.name())).withStyle(ChatFormatting.LIGHT_PURPLE)));
			int i = 0;
			for (Multiset.Entry<VotingOption> e : Multisets.copyHighestCountFirst(options).entrySet()) {
				if (i >= BallotBox.CONFIG.awardLimit.value()) return;
				feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("%d - %s".formatted(e.getCount(), e.getElement().name())).withStyle(ChatFormatting.YELLOW)));
				i++;
			}
		});
		feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("--- End Votes ---").withStyle(ChatFormatting.AQUA)));
		return 0;
	}

	private static int vote(ServerPlayer player, Consumer<Component> feedback) {
		if (player == null) {
			feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("Vote cannot be invoked by a non-player").withStyle(ChatFormatting.RED)));
			return 0;
		}
		if (player.getServer().isSingleplayer()) {
			feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("Voting isn't available in singleplayer!").withStyle(ChatFormatting.RED)));
			return 0;
		}
		if (!ServerPlayNetworking.canSend(player, OpenVoteScreen.ID)) {
			feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("Voting requires BallotBox on the client!").withStyle(ChatFormatting.RED)));
			return 0;
		}
		if (!BallotBox.isOpen()) {
			feedback.accept(Component.literal("[BallotBox] ").withStyle(ChatFormatting.GREEN).append(Component.literal("Voting is unavailable! Voting closed %s.".formatted(BallotBox.relativeTime(BallotBox.closingTime))).withStyle(ChatFormatting.RED)));
			return 0;
		}
		ServerPlayNetworking.send(player, new OpenVoteScreen());
		BallotBoxNetworking.sendVoteScreenData(player);
		return 1;
	}
}
