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
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.client.VotingScreen;
import net.modfest.ballotbox.packet.OpenVoteScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
    @WrapOperation(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 0))
    private static Widget replaceSendFeedback(GridWidget.Adder instance, Widget widget, Operation<Widget> original, Screen parentScreen, GridWidget.Adder gridAdder) {
        if (MinecraftClient.getInstance().isIntegratedServerRunning() || !ClientPlayNetworking.canSend(OpenVoteScreenPacket.ID)) return original.call(instance, widget);
        return gridAdder.add(ButtonWidget.builder(Text.of("Submission Voting"), b -> {
            MinecraftClient.getInstance().setScreen(new VotingScreen());
            ClientPlayNetworking.send(new OpenVoteScreenPacket());
        }).width(98).build());
    }
    @WrapOperation(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 1))
    private static Widget replaceReportBugs(GridWidget.Adder instance, Widget widget, Operation<Widget> original, Screen parentScreen, GridWidget.Adder gridAdder) {
        if (MinecraftClient.getInstance().isIntegratedServerRunning() || !ClientPlayNetworking.canSend(OpenVoteScreenPacket.ID)) return original.call(instance, widget);
        return gridAdder.add(ButtonWidget.builder(Text.of(BallotBox.CONFIG.bug_text.value()), ConfirmLinkScreen.opening(parentScreen, BallotBox.CONFIG.bug_url.value())).width(98).build());
    }
}
