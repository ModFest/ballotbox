package net.modfest.ballotbox.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.client.VotingScreen;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
    @WrapOperation(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;addFeedbackAndBugsButtons(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/gui/widget/GridWidget$Adder;)V"))
    private void replaceSendFeedback(Screen parentScreen, GridWidget.Adder gridAdder, Operation<Void> original) {
        gridAdder.add(ButtonWidget.builder(Text.of("Submission Voting"), b -> {
            MinecraftClient.getInstance().setScreen(new VotingScreen());
            ClientPlayNetworking.send(new OpenVoteScreenPacket());
        }).width(98).build());
        gridAdder.add(ButtonWidget.builder(Text.of(BallotBox.CONFIG.bug_text.value()), ConfirmLinkScreen.opening((GameMenuScreen) (Object) this, BallotBox.CONFIG.bug_url.value())).width(98).build());
    }
}
