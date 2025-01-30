package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.sound.MusicType;
import net.minecraft.text.Text;
import net.modfest.ballotbox.BallotBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@WrapOperation(method = "addNormalWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 2))
	private Element replaceRealms(TitleScreen instance, Element element, Operation<Element> original, int y, int spacingY) {
		if (!BallotBox.CONFIG.replace_realms_credits.value()) return original.call(instance, element);
		return addDrawableChild(ButtonWidget.builder(Text.of(BallotBox.CONFIG.credits_text.value()), b -> {
					MinecraftClient.getInstance().setScreen(new CreditsScreen(false, () -> MinecraftClient.getInstance().setScreen((TitleScreen) (Object) this)));
				MinecraftClient.getInstance().getMusicTracker().stop();
				MinecraftClient.getInstance().getMusicTracker().play(new MusicInstance(MusicType.CREDITS));
				})
			.dimensions(this.width / 2 - 100, y, 200, 20)
			.build()
		);
	}
}
