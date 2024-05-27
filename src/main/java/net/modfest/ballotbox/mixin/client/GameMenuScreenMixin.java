package net.modfest.ballotbox.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.client.VotingScreen;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
    @ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/layout/GridWidget$AdditionHelper;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 2))
    private Widget replaceSendFeedback(Widget original) {
        return ButtonWidget.builder(Text.of("Submission Voting"), b -> {
            MinecraftClient.getInstance().setScreen(new VotingScreen());
            ClientPlayNetworking.send(new OpenVoteScreenPacket());
        }).width(98).build();
    }
    @ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/layout/GridWidget$AdditionHelper;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 3))
    private Widget replaceReportBugs(Widget original) {
        return ButtonWidget.builder(Text.of("ModFest Discord"), ConfirmLinkScreen.createPressAction((GameMenuScreen) (Object) this, BallotBox.CONFIG.discord_url.value())).width(98).build();
    }
}
