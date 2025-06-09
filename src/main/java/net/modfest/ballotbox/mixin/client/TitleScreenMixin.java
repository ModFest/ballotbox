package net.modfest.ballotbox.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.modfest.ballotbox.ButtonActionType;
import net.modfest.ballotbox.client.ApplyModifications;
import net.modfest.ballotbox.client.BallotBoxButtons;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Comparator;

@Mixin(value = TitleScreen.class, priority = 1200)
public abstract class TitleScreenMixin extends Screen implements ApplyModifications {
	@Shadow
	@Nullable
	private RealmsNotificationsScreen realmsNotificationGui;

	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Override
	public void ballotbox$applyModifications() {
		var findButtons = new ArrayList<ButtonWidget>();
		for (var element : ((ScreenAccessor) this).getChildren()) {
			if (element instanceof ButtonWidget widget) {
				findButtons.add(widget);
			}
		}
		findButtons.sort(Comparator.comparing(ClickableWidget::getY));

		for (var pair : BallotBoxButtons.createButtons()) {
			var settings = pair.getLeft();
			if (!settings.apply_in_main_menu.value()) {
				continue;
			}

			if (settings.action_type.value() == ButtonActionType.REPLACE) {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var button = pair.getRight().apply(this).width(child.getWidth()).position(child.getX(), child.getY()).build();
						findButtons.set(i, button);
						this.remove(child);
						if (child.getMessage().getContent() instanceof TranslatableTextContent content && content.getKey().equals("menu.online")) {
							this.realmsNotificationGui = null;
						}
						this.addDrawableChild(button);
						break;
					}
				}
			} else {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					if (BallotBoxButtons.match(child, settings)) {
						var y = settings.action_type.value() == ButtonActionType.INSERT_AFTER ? child.getY() + 24 : child.getY();
						var button = pair.getRight().apply(this).width(200).position(child.getX(), y).build();

						for (int a = i; a < findButtons.size(); a++) {
							child = findButtons.get(a);
							if (child.getY() >= y) {
								child.setY(child.getY() + 24);
							}
						}

						this.addDrawableChild(button);
						findButtons.add(button);
						findButtons.sort(Comparator.comparing(ClickableWidget::getY));
						break;
					}
				}
			}
		}
	}
}
