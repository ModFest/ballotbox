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
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
			boolean found = false;
			var settings = pair.getA();

			Set<String> replace = settings.entrySet().stream().filter(e -> e.getValue() == ButtonActionType.REPLACE).map(Map.Entry::getKey).collect(Collectors.toSet());
			if (!replace.isEmpty()) {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					if (BallotBoxButtons.match(child, replace) != null) {
						var button = pair.getB().apply(this).width(child.getWidth()).pos(child.getX(), child.getY()).build();
						findButtons.set(i, button);
						this.removeWidget(child);
						if (child.getMessage().getContents() instanceof TranslatableContents content && content.getKey().equals("menu.online")) {
							this.realmsNotificationsScreen = null;
						}
						this.addRenderableWidget(button);
						found = true;
						break;
					}
				}
			}
			Set<String> insert = new HashSet<>(settings.keySet());
			insert.removeAll(replace);
			if (!found && !insert.isEmpty()) {
				for (int i = 0; i < findButtons.size(); i++) {
					var child = findButtons.get(i);
					String firstMatch = BallotBoxButtons.match(child, insert);
					if (firstMatch != null) {
						var y = settings.get(firstMatch) == ButtonActionType.INSERT_AFTER ? child.getY() + 24 : child.getY();
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
