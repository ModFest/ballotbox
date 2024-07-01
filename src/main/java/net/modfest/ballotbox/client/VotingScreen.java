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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VotingScreen extends SpruceScreen {
    public static final Text TITLE = Text.literal("ModFest Voting");
    public static final Text LOADING_INDICATOR = Text.literal("Loading...");
    public static final List<String> CATEGORY_TYPES = List.of(
        "theme",
        "community"
    );

    public static final Identifier LOCKUP_TEXTURE = Identifier.of("modfest", "textures/art/graphics/lockup-transparent.png");
    public static final int LOCKUP_TEXTURE_WIDTH = 878;
    public static final int LOCKUP_TEXTURE_HEIGHT = 256;

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
        sidePanelVerticalPadding = (int) (height / 5.5);
        var tabs = new SpruceTabbedWidget(Position.of(this, 0, sidePanelVerticalPadding), width, height - sidePanelVerticalPadding, null, (int) (width / 3.5), 0);
        Map<String, List<VotingCategory>> typedCategories = categories.stream().collect(Collectors.groupingBy(VotingCategory::type));
        typedCategories.entrySet().stream().sorted(Comparator.comparing(e -> CATEGORY_TYPES.contains(e.getKey()) ? CATEGORY_TYPES.indexOf(e.getKey()) : 99)).forEach(e -> {
            e.getValue().forEach(category -> addCategoryTab(tabs, category));
            if (tabs.getList().children().size() < categories.size() + typedCategories.keySet().size() - 1) tabs.addSeparatorEntry(null);
        });
        tabs.getList().setBackground(EmptyBackground.EMPTY_BACKGROUND);
        addSelectableChild(tabs);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        if (loaded) {
            context.fill(0, 0, sidePanelWidth, height, 0x30000000);
        }
    }

    public void renderLockup(DrawContext context) {
        RenderSystem.enableBlend();
        int drawHeight = sidePanelWidth * LOCKUP_TEXTURE_HEIGHT / LOCKUP_TEXTURE_WIDTH;
        context.drawTexture(LOCKUP_TEXTURE, 0, (sidePanelVerticalPadding - drawHeight) / 2, sidePanelWidth, drawHeight, 0, 0, LOCKUP_TEXTURE_WIDTH, LOCKUP_TEXTURE_HEIGHT, LOCKUP_TEXTURE_WIDTH, LOCKUP_TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        if (loaded) {
            super.render(context, mouseX, mouseY, delta);
            renderLockup(context);
            context.drawVerticalLine(sidePanelWidth, 0, height, 0xFFFFFFFF);
        } else {
            int textWidth = textRenderer.getWidth(LOADING_INDICATOR);
            context.drawText(textRenderer, LOADING_INDICATOR, width - textWidth - 10, height - 15, 0xFFFFFFFF, true);
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
            selections.putAll(packet.selections().votes());
            previousSelections.clear();
            previousSelections.putAll(packet.selections().votes());
            categories.clear();
            categories.addAll(packet.categories());
            options.clear();
            options.addAll(packet.options());
            loaded = true;
            initLoaded();
        }
    }

    public class VotingOptionButtonWidget extends SpruceButtonWidget {
        public static final Identifier VOTED_TEXTURE = Identifier.of(BallotBox.ID, "button_voted");
        public static final Identifier CHECKMARK_TEXTURE = Identifier.of(BallotBox.ID, "textures/gui/button_checkmark.png");

        public final CategoryContainerWidget parent;
        public boolean selected;
        public boolean prohibited;
        public String url;
        Identifier texture = null;

        public VotingOptionButtonWidget(Position position, int width, int height, VotingCategory category, VotingOption option, CategoryContainerWidget parent, boolean prohibited) {
            super(position, width, height, Text.literal(option.name()), button -> {
                if (button instanceof VotingOptionButtonWidget votingButton) {
                    if (selections.containsEntry(category.id(), option.id())) {
                        selections.remove(category.id(), option.id());
                        votingButton.selected = false;
                    } else {
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
            FabricLoader.getInstance().getModContainer(option.id()).ifPresent(mod -> {
                mod.getMetadata().getIconPath(16).ifPresent(iconPath -> mod.findPath(iconPath).ifPresent(path -> {
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        texture = Identifier.of(BallotBox.ID, mod.getMetadata().getId() + "_icon");
                        this.client.getTextureManager().registerTexture(texture, new NativeImageBackedTexture(NativeImage.read(inputStream)));
                    } catch (IOException ignored) {
                    }
                }));
            });
            if (option.platform().type().equals("modrinth")) url = "https://modrinth.com/mod/%s".formatted(option.platform().project_id()); // Use project ID later
            setTooltip(url == null ? Text.literal(option.description()).formatted(Formatting.GRAY) : Text.literal(option.description()).formatted(Formatting.GRAY).append(Text.literal("\n")).append(Text.literal("Right-Click").formatted(Formatting.GOLD)).append(Text.literal(" to open the mod page.").formatted(Formatting.WHITE)));
        }

        @Override
        public boolean isActive() {
            return !prohibited && super.isActive();
        }

        @Override
        public Optional<Text> getTooltip() {
            return isActive() ? super.getTooltip() : prohibited ? Optional.of(Text.literal("Prohibited by another category!").formatted(Formatting.GRAY)) : Optional.of(Text.literal("You've reached the category vote limit!").formatted(Formatting.GRAY));
        }

        @Override
        protected boolean onMouseClick(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_2 && url != null) {
                Util.getOperatingSystem().open(url); // confirmation screen causes save
                return true;
            }
            return super.onMouseClick(mouseX, mouseY, button);
        }

        @Override
        protected Identifier getTexture() {
            if (selected) {
                return VOTED_TEXTURE;
            }
            return super.getTexture();
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            int textWidth = client.textRenderer.getWidth(getMessage());
            int left = getX() + 2, right = getX() + getWidth() - 2;
            int bottom = getY() + getHeight();
            int textY = (getY() * 2 + getHeight() - 9) / 2 + 1;
            if (texture != null) {
                context.drawTexture(texture, left, getY() + 2, 16, 16, 0, 0, 16, 16, 16, 16);
            }
            if (textWidth <= getWidth()) {
                context.drawCenteredTextWithShadow(client.textRenderer, getMessage(), left + getWidth() / 2, textY, 0xFFFFFFFF);
                return;
            }
            int extraWidth = textWidth - getWidth();
            double seconds = (double) Util.getMeasuringTimeMs() / 1000.0;
            double clampedWidth = Math.max(extraWidth * 0.5, 3.0);
            double scroll = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2) * seconds / clampedWidth)) / 2.0 + 0.5;
            double offset = MathHelper.lerp(scroll, 0.0, extraWidth);
            context.enableScissor(left, Math.max(getY(), parent.getY()), right, bottom);
            context.drawTextWithShadow(client.textRenderer, getMessage(), left - (int) offset, textY, 0xFFFFFFFF);
            context.disableScissor();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            if (selected) {
                context.drawTexture(CHECKMARK_TEXTURE, getX() + getWidth() - 11, getY() + getHeight() - 9, 0, 0, 7, 6, 7, 6);
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.enableScissor(parent.getX(), parent.getY(), parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight());
            super.render(context, mouseX, mouseY, delta);
            context.disableScissor();
        }
    }

    public class CategoryContainerWidget extends SpruceContainerWidget {
        public final VotingCategory category;

        public Map<String, VotingOptionButtonWidget> buttons = new HashMap<>();
        public Text titleText;

        public CategoryContainerWidget(Position position, int width, int height, VotingCategory category) {
            super(position, width, height);
            this.category = category;
            init();
        }

        public void init() {
            List<String> prohibitedIds = new ArrayList<>();
            category.prohibitions().ifPresent(prohibitions -> prohibitions.forEach(prohibition -> prohibitedIds.addAll(selections.get(prohibition))));
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
        }

        public void updateSelections() {
            int selected = selections.get(category.id()).size();
            boolean atLimit = selected >= category.limit();
            titleText = Text.literal(category.name()).append(Text.literal(" (" + selected + "/" + category.limit() + ")").formatted(atLimit ? Formatting.GREEN : Formatting.GRAY));
            buttons.forEach((id, button) -> button.setActive(button.selected || !atLimit));
        }

        public void updateProhibitions(String optionId, boolean selected) {
            category.prohibitions().ifPresent(prohibitions -> {
                for (var prohibition : prohibitions) {
                    categoryWidgets.get(prohibition).buttons.get(optionId).prohibited = selected;
                }
            });
        }

        public float drawTitleText(DrawContext context) {
            int titleWidth = client.textRenderer.getWidth(titleText);
            float titleScale = Math.min((float) (width - 20) / titleWidth, 2.0f);
            context.getMatrices().push();
            context.getMatrices().translate(getPosition().getX() + 10, 10, 0);
            context.getMatrices().scale(titleScale, titleScale, 1.0f);
            context.drawText(client.textRenderer, titleText, 0, 0, 0xFFFFFFFF, true);
            context.getMatrices().pop();
            return titleScale;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            float titleScale = drawTitleText(context);
            context.drawText(client.textRenderer, Text.literal(category.description()), getPosition().getX() + 10, 15 + (int) (9 * titleScale), 0xFFFFFFFF, true);
            super.renderWidget(context, mouseX, mouseY, delta);
        }
    }
}
