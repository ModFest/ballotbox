package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.sound.MusicType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.client.BallotBoxClient;
import net.modfest.ballotbox.client.VotingScreen;
import net.modfest.ballotbox.packet.OpenVoteScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
	private static ButtonWidget ballotbox$voteButton = null;

	@WrapOperation(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 0))
	private static Widget replaceSendFeedback(GridWidget.Adder instance, Widget widget, Operation<Widget> original, Screen parentScreen) {
		if (!BallotBox.CONFIG.replace_feedback.value() || !BallotBoxClient.isEnabled(MinecraftClient.getInstance())) return original.call(instance, widget);
		ballotbox$voteButton = ButtonWidget.builder(Text.of("Submission Voting"), b -> {
			MinecraftClient.getInstance().setScreen(new VotingScreen());
			ClientPlayNetworking.send(new OpenVoteScreen());
		}).width(98).tooltip(BallotBoxClient.isOpen() ? null : Tooltip.of(Text.literal("Closed %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).formatted(Formatting.GRAY))).build();
		ballotbox$voteButton.active = BallotBoxClient.isOpen();
		return instance.add(ballotbox$voteButton);
	}

	@WrapOperation(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 1))
	private static Widget replaceReportBugs(GridWidget.Adder instance, Widget widget, Operation<Widget> original, Screen parentScreen) {
		if (!BallotBox.CONFIG.replace_bugs.value() || !BallotBoxClient.isEnabled(MinecraftClient.getInstance())) return original.call(instance, widget);
		return instance.add(ButtonWidget.builder(Text.of(BallotBox.CONFIG.bug_text.value()), ConfirmLinkScreen.opening(parentScreen, BallotBox.CONFIG.bug_url.value())).width(98).build());
	}

	@WrapOperation(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 6))
	private Widget replacePlayerReporting(GridWidget.Adder instance, Widget widget, Operation<Widget> original) {
		if (!BallotBox.CONFIG.replace_reporting_credits.value()) return original.call(instance, widget);
		return instance.add(ButtonWidget.builder(Text.of(BallotBox.CONFIG.credits_text.value()), b -> {
			MinecraftClient.getInstance().setScreen(new CreditsScreen(false, () -> MinecraftClient.getInstance().setScreen((GameMenuScreen) (Object) this)));
			MinecraftClient.getInstance().getMusicTracker().stop();
			MinecraftClient.getInstance().getMusicTracker().play(new MusicInstance(MusicType.CREDITS));
		}).width(98).build());
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void addReminder(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (ballotbox$voteButton == null) return;
		ballotbox$voteButton.active = BallotBoxClient.isOpen();
		if (BallotBoxClient.isOpen() && BallotBoxClient.remainingVotes > 0) {
			Text remainingText = Text.literal("%s vote%s available!".formatted(BallotBoxClient.remainingVotes, BallotBoxClient.remainingVotes > 1 ? "s" : "")).formatted(Formatting.GREEN);
			context.drawText(MinecraftClient.getInstance().textRenderer, remainingText, ballotbox$voteButton.getX() - MinecraftClient.getInstance().textRenderer.getWidth(remainingText) - 2, ballotbox$voteButton.getY() + 2, 0xFFFFFFFF, true);
			if (BallotBoxClient.closingTime != null) {
				Text timeText = Text.literal("Closes %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).formatted(Formatting.YELLOW);
				context.drawText(MinecraftClient.getInstance().textRenderer, timeText, ballotbox$voteButton.getX() - MinecraftClient.getInstance().textRenderer.getWidth(timeText) - 2, ballotbox$voteButton.getY() + 10, 0xFFFFFFFF, true);
			}
		}
	}
}
