package net.modfest.ballotbox.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.client.ApplyModifications;
import net.modfest.ballotbox.client.BallotBoxButtons;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.util.ArrayList;
import java.util.Comparator;

@Mixin(value = TitleScreen.class, priority = 1200)
public abstract class TitleScreenMixin extends Screen implements ApplyModifications {
	@Shadow
	@Nullable
	private RealmsNotificationsScreen realmsNotificationsScreen;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Override
	public void ballotbox$applyModifications() {
		var findButtons = new ArrayList<Button>();
		for (var element : ((ScreenAccessor) this).getChildren()) {
			if (element instanceof Button widget) {
				findButtons.add(widget);
			}
		}
		findButtons.sort(Comparator.comparing(AbstractWidget::getY));

		for (var pair : BallotBoxButtons.createButtons()) {
			var settings = pair.getA();
			if (!settings.apply_in_main_menu.value()) {
				continue;
			}

			if (settings.action_type.value() == ButtonActionType.REPLACE) {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var button = pair.getB().apply(this).width(child.getWidth()).pos(child.getX(), child.getY()).build();
						findButtons.set(i, button);
						this.removeWidget(child);
						if (child.getMessage().getContents() instanceof TranslatableContents content && content.getKey().equals("menu.online")) {
							this.realmsNotificationsScreen = null;
						}
						this.addRenderableWidget(button);
						break;
					}
				}
			} else {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var y = settings.action_type.value() == ButtonActionType.INSERT_AFTER ? child.getY() + 24 : child.getY();
						var button = pair.getB().apply(this).width(200).pos(child.getX(), y).build();

						for (int a = i; a < findButtons.size(); a++) {
							child = findButtons.get(a);
							if (child.getY() >= y) {
								child.setY(child.getY() + 24);
							}
						}

						this.addRenderableWidget(button);
						findButtons.add(button);
						findButtons.sort(Comparator.comparing(AbstractWidget::getY));
						break;
					}
				}
			}
		}
	}
}
