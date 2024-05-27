package net.modfest.ballotbox.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.background.EmptyBackground;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.client.packet.C2SUpdateVote;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.mixin.client.OptionEntryAccessor;
import net.modfest.ballotbox.packet.S2CVoteScreenData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingScreen extends SpruceScreen {
    public static final Text TITLE = Text.literal("ModFest Voting");
    public static final Text LOADING_INDICATOR = Text.literal("Loading...");

    // FIXME: Placeholder
    public static final Identifier LOCKUP_TEXTURE = new Identifier("modfest", "textures/art/graphics/lockup-transparent.png");
    public static final int LOCKUP_TEXTURE_WIDTH = 1129; //141;
    public static final int LOCKUP_TEXTURE_HEIGHT = 256; //32;

    protected final Multimap<String, String> previousSelections = HashMultimap.create();
    protected final Multimap<String, String> selections = HashMultimap.create();
    protected List<VotingCategory> categories = new ArrayList<>();
    protected List<VotingOption> options = new ArrayList<>();
    protected boolean loaded = false;

    protected int sidePanelWidth;
    protected int sidePanelVerticalPadding;
    protected Map<String, CategoryContainerWidget> categoryWidgets = new HashMap<>();

    public VotingScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        if (loaded) {
            initLoaded();
        }
    }

    protected void initLoaded() {
        initSidePanel();
    }

    protected void addCategoryTab(SpruceTabbedWidget tabs, VotingCategory category) {
        tabs.addTabEntry(Text.literal(category.name()), null, (w, h) -> {
            var categoryWidget = new CategoryContainerWidget(Position.origin(), w, h, category);
            categoryWidgets.put(category.id(), categoryWidget);
            return categoryWidget;
        });
    }

    protected void initSidePanel() {
        sidePanelWidth = (int) (width / 3.5);
        sidePanelVerticalPadding = (int) (height / 5.0);
        var tabs = new SpruceTabbedWidget(Position.of(this, 0, sidePanelVerticalPadding), width, height - sidePanelVerticalPadding, null, (int) (width / 3.5), 0);
        // TODO: Clean up!
        for (var themeCategory : categories.stream().filter(cat -> cat.type().equals("theme")).toList()) {
            addCategoryTab(tabs, themeCategory);
        }
        tabs.addSeparatorEntry(null);
        for (var communityCategory : categories.stream().filter(cat -> cat.type().equals("community")).toList()) {
            addCategoryTab(tabs, communityCategory);
        }
        tabs.getList().setBackground(EmptyBackground.EMPTY_BACKGROUND);
        addDrawableSelectableElement(tabs);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        if (loaded) {
            graphics.fill(0, 0, sidePanelWidth, height, 0x30000000);
        }
    }

    public void renderLockup(GuiGraphics graphics) {
        RenderSystem.enableBlend();
        int drawHeight = sidePanelWidth * LOCKUP_TEXTURE_HEIGHT / LOCKUP_TEXTURE_WIDTH;
        graphics.drawTexture(LOCKUP_TEXTURE, 0, (sidePanelVerticalPadding - drawHeight) / 2, sidePanelWidth, drawHeight, 0, 0, LOCKUP_TEXTURE_WIDTH, LOCKUP_TEXTURE_HEIGHT, LOCKUP_TEXTURE_WIDTH, LOCKUP_TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        if (loaded) {
            super.render(graphics, mouseX, mouseY, delta);
            renderLockup(graphics);
            graphics.drawVerticalLine(sidePanelWidth, 0, height, 0xFFFFFFFF);
        } else {
            int textWidth = textRenderer.getWidth(LOADING_INDICATOR);
            graphics.drawText(textRenderer, LOADING_INDICATOR, width - textWidth - 10, height - 15, 0xFFFFFFFF, true);
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
            loaded = true;
            initLoaded();
        }
    }

    public class VotingOptionButtonWidget extends SpruceButtonWidget {
        public static final Identifier VOTED_TEXTURE = new Identifier(BallotBox.ID, "button_voted");
        public static final Identifier CHECKMARK_TEXTURE = new Identifier(BallotBox.ID, "textures/gui/button_checkmark.png");

        public final CategoryContainerWidget parent;
        public boolean selected;
        public boolean prohibited;

        public VotingOptionButtonWidget(Position position, int width, int height, VotingCategory category, VotingOption option, CategoryContainerWidget parent, boolean prohibited) {
            super(position, width, height, Text.literal(option.name()), button -> {
                if (button instanceof VotingOptionButtonWidget votingButton) {
                    if (selections.containsEntry(category.id(), option.id())) {
                        selections.remove(category.id(), option.id());
                        votingButton.selected = false;
                    }
                    else {
                        selections.put(category.id(), option.id());
                        votingButton.selected = true;
                    }
                    parent.updateSelections();
                    parent.updateProhibitions(option.id(), votingButton.selected);
                }
            });
            this.parent = parent;
            selected = selections.containsEntry(category.id(), option.id());
            this.prohibited = prohibited;
            if (prohibited && isActive()) {
                setActive(false);
            }
        }

        @Override
        protected Identifier getTexture() {
            if (selected) {
                return VOTED_TEXTURE;
            }
            return super.getTexture();
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int textWidth = client.textRenderer.getWidth(getMessage());
            int left = getX() + 2, right = getX() + getWidth() - 2;
            int bottom = getY() + getHeight();
            int textY = (getY() * 2 + getHeight() - 9) / 2 + 1;
            if (textWidth <= getWidth()) {
                graphics.drawCenteredShadowedText(client.textRenderer, getMessage(), left + getWidth() / 2, textY, 0xFFFFFFFF);
                return;
            }
            int extraWidth = textWidth - getWidth();
            double seconds = (double) Util.getMeasuringTimeMs() / 1000.0;
            double clampedWidth = Math.max(extraWidth * 0.5, 3.0);
            double scroll = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2) * seconds / clampedWidth)) / 2.0 + 0.5;
            double offset = MathHelper.lerp(scroll, 0.0, extraWidth);
            graphics.enableScissor(left, Math.max(getY(), parent.getY()), right, bottom);
            graphics.drawShadowedText(client.textRenderer, getMessage(), left - (int)offset, textY, 0xFFFFFFFF);
            graphics.disableScissor();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(graphics, mouseX, mouseY, delta);
            if (selected) {
                graphics.drawTexture(CHECKMARK_TEXTURE, getX() + getWidth() - 11, getY() + getHeight() - 9, 0, 0, 7, 6, 7, 6);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.enableScissor(parent.getX(), parent.getY(), parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight());
            super.render(graphics, mouseX, mouseY, delta);
            graphics.disableScissor();
        }
    }

    public class CategoryContainerWidget extends SpruceContainerWidget {
        public final VotingCategory category;

        public Map<String, VotingOptionButtonWidget> buttons = new HashMap<>();
        public Text titleText;
        public boolean atLimit = false;

        public CategoryContainerWidget(Position position, int width, int height, VotingCategory category) {
            super(position, width, height);
            this.category = category;
            init();
        }

        public void init() {
            List<String> prohibitedIds = new ArrayList<>();
            category.prohibitions().ifPresent(prohibitions -> prohibitions.forEach(prohibition -> {
                prohibitedIds.addAll(selections.get(prohibition));
            }));
            addChildren((containerWidth, containerHeight, widgetAdder) -> {
                var optionList = new SpruceOptionListWidget(Position.of(this, 1, 0), containerWidth, containerHeight);
                for (var optionPairs : Lists.partition(options, 2)) {
                    var listEntry = OptionEntryAccessor.ballotbox$create(optionList);
                    for (int i = 0; i < optionPairs.size(); i++) {
                        var option = optionPairs.get(i);
                        int buttonWidth = (int) (containerWidth / 2.3);
                        int buttonX = containerWidth / 2 + (i == 0 ? -buttonWidth - 7 : 7);
                        var button = new VotingOptionButtonWidget(Position.of(listEntry, buttonX, 0), buttonWidth, 20, category, option, this, prohibitedIds.contains(option.id()));
                        listEntry.children().add(button);
                        buttons.put(option.id(), button);
                    }
                    optionList.children().add(listEntry);
                }
                optionList.setBackground(EmptyBackground.EMPTY_BACKGROUND);
                optionList.setRenderTransition(false);
                widgetAdder.accept(optionList);
            });
            updateSelections();
            updateProhibitions();
        }

        public void updateSelections() {
            boolean wasAtLimit = atLimit;
            int selected = selections.get(category.id()).size();
            atLimit = selected >= category.limit();
            titleText = Text.literal(category.name()).append(Text.literal(" (" + selected + "/" + category.limit() + ")").formatted(atLimit ? Formatting.GREEN : Formatting.GRAY));
            if (atLimit != wasAtLimit) {
                for (var entry : buttons.entrySet()) {
                    if (!entry.getValue().selected && !entry.getValue().prohibited) {
                        entry.getValue().setActive(!atLimit);
                    }
                }
            }
        }

        public void updateProhibitions() {
            for (VotingCategory category : categories) {
                if (category.prohibitions().isPresent() && category.prohibitions().get().contains(category.id())) {
                    for (String option : selections.get(category.id())) {
                        buttons.get(option).setActive(false);
                    }
                }
            }
        }

        public void updateProhibitions(String optionId, boolean selected) {
            category.prohibitions().ifPresent(prohibitions -> {
                for (var prohibition : prohibitions) {
                    categoryWidgets.get(prohibition).buttons.get(optionId).setActive(!selected);
                }
            });
        }

        public float drawTitleText(GuiGraphics graphics) {
            int titleWidth = client.textRenderer.getWidth(titleText);
            float titleScale = Math.min((float) (width - 20) / titleWidth, 2.0f);
            graphics.getMatrices().push();
            graphics.getMatrices().translate(getPosition().getX() + 10, 10, 0);
            graphics.getMatrices().scale(titleScale, titleScale, 1.0f);
            graphics.drawText(client.textRenderer, titleText, 0, 0, 0xFFFFFFFF, true);
            graphics.getMatrices().pop();
            return titleScale;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            float titleScale = drawTitleText(graphics);
            graphics.drawText(client.textRenderer, Text.literal(category.description()), getPosition().getX() + 10, 15 + (int) (9 * titleScale), 0xFFFFFFFF, true);
            super.renderWidget(graphics, mouseX, mouseY, delta);
        }
    }
}
