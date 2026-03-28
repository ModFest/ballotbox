package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.client.BallotBoxButtons;
import net.modfest.ballotbox.client.BallotBoxClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PauseScreen.class, priority = 1200)
public abstract class GameMenuScreenMixin extends Screen {
	private static Button ballotbox$voteButton = null;

	protected GameMenuScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"))
	private void onInitWidgets(CallbackInfo ci, @Local GridLayout instance) {
		var reorganize = false;
		var children = ((GridLayoutAccessor) instance).getChildren();
		for (var pair : BallotBoxButtons.createButtons()) {
			var settings = pair.getA();
			if (!settings.apply_in_pause_screen.value()) {
				continue;
			}

			if (settings.action_type.value() == ButtonActionType.REPLACE) {
				for (int i = 0; i < children.size(); i++) {
					var childContainer = children.get(i);
					var child = childContainer.child;
					var containerAccessor = (ChildContainerAccessor) childContainer;

					if (BallotBoxButtons.match(child, settings)) {
						var button = pair.getB().apply(this).width(child.getWidth()).pos(child.getX(), child.getY()).build();
						if (settings == BallotBox.CONFIG.voting_button) {
							ballotbox$voteButton = button;
						}

						// funky modmenu friendliness hack
						if (child instanceof AbstractWidget widget && widget.getMessage().getContents() instanceof TranslatableContents trans && trans.getKey().equals("menu.reportBugs")) {
							i--;
						}
						// disable instead of delete so modmenu can still reference for position
						((AbstractWidget) child).active = false;
						((AbstractWidget) child).visible = false;

						children.add(i, new GridLayout.ChildContainer(
							button,
							containerAccessor.getRow(), containerAccessor.getColumn(),
							containerAccessor.getOccupiedRows(), containerAccessor.getOccupiedColumns(),
							childContainer.layoutSettings
						));
						break;
					}
				}
			} else {
				reorganize = true;

				for (int i = 0; i < children.size(); i++) {
					var child = children.get(i).child;
					if (BallotBoxButtons.match(child, settings)) {
						var after = settings.action_type.value() == ButtonActionType.INSERT_AFTER;

						var button = pair.getB().apply(this).width(204).build();
						if (settings == BallotBox.CONFIG.voting_button) {
							ballotbox$voteButton = button;
						}
						var insertRow = ((ChildContainerAccessor) children.get(i)).getRow() + (after ? 1 : 0);
						for (int j = 0; j < children.size(); j++) {
							var chjld = (ChildContainerAccessor)children.get(j);
							if (chjld.getRow() >= insertRow) {
								chjld.setRow(chjld.getRow() + 1);
							}
						}
						instance.addChild(button, insertRow,  0, 1, 2);
						break;
					}
				}
			}
		}
		if (reorganize) {
			instance.arrangeElements();
			FrameLayout.alignInRectangle(instance, 0, 0, this.width, this.height, 0.5F, 0.25F);
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void addReminder(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (ballotbox$voteButton == null) return;
		ballotbox$voteButton.active = BallotBoxClient.isOpen() && BallotBoxClient.isEnabled(Minecraft.getInstance());
		if (BallotBoxClient.isOpen() && BallotBoxClient.remainingVotes > 0) {
			int xOffset = BallotBox.CONFIG.reminder_settings.reminder_x_offset.value();
			int yOffset = BallotBox.CONFIG.reminder_settings.reminder_y_offset.value();
			Component remainingText = Component.literal("%s vote%s available!".formatted(BallotBoxClient.remainingVotes, BallotBoxClient.remainingVotes > 1 ? "s" : "")).withStyle(ChatFormatting.GREEN);
			context.text(Minecraft.getInstance().font, remainingText, ballotbox$voteButton.getX() - Minecraft.getInstance().font.width(remainingText) - 2 + xOffset, ballotbox$voteButton.getY() + 2 + yOffset, 0xFFFFFFFF, true);
			if (BallotBoxClient.closingTime != null) {
				Component timeText = Component.literal("Closes %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).withStyle(ChatFormatting.YELLOW);
				context.text(Minecraft.getInstance().font, timeText, ballotbox$voteButton.getX() - Minecraft.getInstance().font.width(timeText) - 2 + xOffset, ballotbox$voteButton.getY() + 10 + yOffset, 0xFFFFFFFF, true);
			}
		}
	}
}
