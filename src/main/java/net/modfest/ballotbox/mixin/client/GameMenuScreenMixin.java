package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.client.BallotBoxButtons;
import net.modfest.ballotbox.client.BallotBoxClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = PauseScreen.class, priority = 1200)
public abstract class GameMenuScreenMixin extends Screen {
	private static Button ballotbox$voteButton = null;

	protected GameMenuScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"))
	private void onInitWidgets(CallbackInfo ci, @Local GridLayout instance) {
		var reorganize = false;
		var children = ((GridWidgetAccessor) instance).getChildren();
		var grids = ((GridWidgetAccessor) instance).getCellInhabitants();
		for (var pair : BallotBoxButtons.createButtons()) {
			var settings = pair.getA();
			if (!settings.apply_in_pause_screen.value()) {
				continue;
			}

			if (settings.action_type.value() == ButtonActionType.REPLACE) {
				for (int i = 0; i < children.size(); i++) {
					var child = children.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var button = pair.getB().apply(this).width(child.getWidth()).pos(child.getX(), child.getY()).build();
						if (settings == BallotBox.CONFIG.voting_button) {
							ballotbox$voteButton = button;
						}
						children.set(i, button);
						break;
					}
				}
			} else {
				reorganize = true;
				if (children.size() != grids.size()) {
					// State is broken! Assume it's modmenu breaking it.
					for (int i = 1; i < children.size(); i++) {
						var child = children.get(i);
						if (child.getClass().getName().equals("com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget")) {
							children.remove(i);
							instance.addChild(child, ((ElementAccessor) grids.get(i - 1)).getRow() + 1, 0, 1, 2);
							i++;
							var newChild = children.removeLast();
							var grid = grids.removeLast();
							children.add(i, newChild);
							grids.add(i, grid);

							for (i++; i < grids.size(); i++) {
								grid = grids.get(i);
								((ElementAccessor) grid).setRow(((ElementAccessor) grid).getRow() + 2);
							}

							break;
						} else if (child.getClass().getName().equals("com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget")) {
							children.remove(i);
							this.addRenderableWidget((ImageButton) child);
							break;
						}
					}
				}

				for (int i = 0; i < children.size(); i++) {
					var child = children.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var after = settings.action_type.value() == ButtonActionType.INSERT_AFTER;
						var currentGrid = grids.get(i);
						while (after && i < children.size() - 1 && ((ElementAccessor) currentGrid).getRow() == ((ElementAccessor) grids.get(i)).getRow()) {
							i++;
						}
						while (!after && i > 1 && ((ElementAccessor) currentGrid).getRow() == ((ElementAccessor) grids.get(i - 1)).getRow()) {
							--i;
						}

						var button = pair.getB().apply(this).width(204).build();
						if (settings == BallotBox.CONFIG.voting_button) {
							ballotbox$voteButton = button;
						}
						var isLast = i == children.size() - 1;
						instance.addChild(button, ((ElementAccessor) grids.get(i)).getRow() + (after ? 1 : 0),  0, 1, 2);
						if (isLast) {
							break;
						}
						var newChild = children.removeLast();
						var grid = grids.removeLast();
						children.add(i, newChild);
						grids.add(i, grid);
						i++;
						for (; i < grids.size(); i++) {
							grid = grids.get(i);
							((ElementAccessor) grid).setRow(((ElementAccessor) grid).getRow() + 2);
						}
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

	@Inject(method = "render", at = @At("TAIL"))
	private void addReminder(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (ballotbox$voteButton == null) return;
		ballotbox$voteButton.active = BallotBoxClient.isOpen();
		if (BallotBoxClient.isOpen() && BallotBoxClient.remainingVotes > 0) {
			int xOffset = BallotBox.CONFIG.reminder_settings.reminder_x_offset.value();
			int yOffset = BallotBox.CONFIG.reminder_settings.reminder_y_offset.value();
			Component remainingText = Component.literal("%s vote%s available!".formatted(BallotBoxClient.remainingVotes, BallotBoxClient.remainingVotes > 1 ? "s" : "")).withStyle(ChatFormatting.GREEN);
			context.drawString(Minecraft.getInstance().font, remainingText, ballotbox$voteButton.getX() - Minecraft.getInstance().font.width(remainingText) - 2 + xOffset, ballotbox$voteButton.getY() + 2 + yOffset, 0xFFFFFFFF, true);
			if (BallotBoxClient.closingTime != null) {
				Component timeText = Component.literal("Closes %s.".formatted(BallotBox.relativeTime(BallotBoxClient.closingTime))).withStyle(ChatFormatting.YELLOW);
				context.drawString(Minecraft.getInstance().font, timeText, ballotbox$voteButton.getX() - Minecraft.getInstance().font.width(timeText) - 2 + xOffset, ballotbox$voteButton.getY() + 10 + yOffset, 0xFFFFFFFF, true);
			}
		}
	}
}
