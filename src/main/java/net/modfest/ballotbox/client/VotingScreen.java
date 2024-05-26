package net.modfest.ballotbox.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.util.ArrayList;
import java.util.List;

public class VotingScreen extends SpruceScreen {
    public static final Text TITLE = Text.literal("Modfest Voting");
    public static final Identifier LOCKUP_TEXTURE = new Identifier("modfest", "textures/art/graphics/lockup-transparent.png");
    public static final int LOCKUP_TEXTURE_WIDTH = 141;
    public static final int LOCKUP_TEXTURE_HEIGHT = 32;
    protected final Multimap<String, String> previousSelections = HashMultimap.create();
    protected final Multimap<String, String> selections = HashMultimap.create();
    protected List<VotingCategory> categories = new ArrayList<>();
    protected List<VotingOption> options = new ArrayList<>();
    protected boolean loaded = false;

    public VotingScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        // TODO: Create all universal children (event logo texture from respack, category scroll list, panes, save-looking exit button)
        addSelectableElement(ButtonWidget.builder(Text.literal("Log State"), b -> {
            NotBallotBoxClient.LOGGER.info("{} categories {} options {} selections", categories.size(), options.size(), selections.size());
        }).position(0, 0).build());
        addSelectableElement(ButtonWidget.builder(Text.literal("Select Bingo"), b -> {
            selections.put("Best Everything Award", "Bingo");
        }).position(0, 30).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (loaded) {
            super.render(graphics, mouseX, mouseY, delta);
        } else {
            renderBackground(graphics, mouseX, mouseY, delta);
            graphics.drawText(textRenderer, Text.literal("Loading..."), width - 50, height - 10, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void removed() {
        if (!previousSelections.equals(selections)) {
            ClientPlayNetworking.send(new C2SUpdateVote(new VotingSelections(selections)));
        }
        super.removed();
    }

    public void load(S2CVoteScreenData packet) {
        if (!loaded) {
            selections.clear();
            selections.putAll(packet.selections().selections());
            previousSelections.clear();
            previousSelections.putAll(packet.selections().selections());
            categories.clear();
            categories.addAll(packet.categories());
            options.clear();
            options.addAll(packet.options());
            // TODO: Create/populate any child components as needed (option lists, category tabs).
            loaded = true;
        }
    }
}
