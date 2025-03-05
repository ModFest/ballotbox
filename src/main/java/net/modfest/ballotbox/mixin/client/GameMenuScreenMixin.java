package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.client.BallotBoxButtons;
import net.modfest.ballotbox.client.BallotBoxClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = GameMenuScreen.class, priority = 1200)
public abstract class GameMenuScreenMixin extends Screen {
	private static ButtonWidget ballotbox$voteButton = null;

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V"))
	private void onInitWidgets(CallbackInfo ci, @Local GridWidget instance) {
		var reorganize = false;
		var children = ((GridWidgetAccessor) instance).getChildren();
		var grids = ((GridWidgetAccessor) instance).getGrids();
		for (var pair : BallotBoxButtons.createButtons()) {
			var settings = pair.getLeft();
			if (!settings.apply_in_pause_screen.value()) {
				continue;
			}

			if (settings.action_type.value() == ButtonActionType.REPLACE) {
				for (int i = 0; i < children.size(); i++) {
					var child = children.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var button = pair.getRight().apply(this).width(child.getWidth()).position(child.getX(), child.getY()).build();
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
							instance.add(child, ((ElementAccessor) grids.get(i - 1)).getRow() + 1, 0, 1, 2);
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
							this.addDrawableChild((TexturedButtonWidget) child);
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

						var button = pair.getRight().apply(this).width(204).build();
						if (settings == BallotBox.CONFIG.voting_button) {
							ballotbox$voteButton = button;
						}
						var isLast = i == children.size() - 1;
						instance.add(button, ((ElementAccessor) grids.get(i)).getRow() + (after ? 1 : 0),  0, 1, 2);
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
			instance.refreshPositions();
			SimplePositioningWidget.setPos(instance, 0, 0, this.width, this.height, 0.5F, 0.25F);
		}
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
