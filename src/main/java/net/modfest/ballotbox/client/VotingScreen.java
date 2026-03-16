package net.modfest.ballotbox.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.background.EmptyBackground;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.modfest.ballotbox.BallotBox;
import net.modfest.ballotbox.data.VotingCategory;
import net.modfest.ballotbox.data.VotingOption;
import net.modfest.ballotbox.data.VotingSelections;
import net.modfest.ballotbox.mixin.client.OptionEntryAccessor;
import net.modfest.ballotbox.packet.C2SUpdateVote;
import net.modfest.ballotbox.packet.S2CVoteScreenData;
import net.modfest.ballotbox.util.ModMetaUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VotingScreen extends SpruceScreen {
	public static final Component TITLE = Component.literal("ModFest Voting");
	public static final Component LOADING_INDICATOR = Component.literal("Loading...");
	public static final List<String> CATEGORY_TYPES = List.of(
		"theme",
		"community"
	);

	public static final ResourceLocation LOCKUP_TEXTURE = ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "emblem");

	protected final Multimap<String, String> previousSelections = HashMultimap.create();
	protected final Multimap<String, String> selections = HashMultimap.create();
	protected List<VotingCategory> categories = new ArrayList<>();
	protected List<VotingOption> options = new ArrayList<>();
	protected boolean loaded = false;
	protected TextureAtlasSprite lockupSprite = null;

	protected int sidePanelWidth;
	protected int sidePanelVerticalPadding;
	protected Map<String, CategoryContainerWidget> categoryWidgets = new ConcurrentHashMap<>();

	private final Map<String, ResourceLocation> modIconCache = new ConcurrentHashMap<>();

	public VotingScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		super.init();
		this.lockupSprite = Minecraft.getInstance().getGuiSprites().getSprite(LOCKUP_TEXTURE);
		if (loaded) {
			initLoaded();
		}
	}

	protected void initLoaded() {
		initSidePanel();
	}

	protected void addCategoryTab(SpruceTabbedWidget tabs, VotingCategory category) {
		tabs.addTabEntry(Component.literal(category.name()), null, (w, h) -> {
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
			if (tabs.getList().children().size() < categories.size() + typedCategories.size() - 1) {
				tabs.addSeparatorEntry(null);
			}
		});
		tabs.getList().setBackground(EmptyBackground.EMPTY_BACKGROUND);
		addRenderableWidget(tabs);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		if (loaded) {
			context.fill(0, 0, sidePanelWidth, height, 0x30000000);
		}
	}

	public void renderLockup(GuiGraphics context) {
		if (lockupSprite == null) return;
		int texHeight = lockupSprite.contents().height();
		int texWidth = lockupSprite.contents().width();
		int drawHeight = sidePanelWidth * texHeight / texWidth;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, LOCKUP_TEXTURE, 0, (sidePanelVerticalPadding - drawHeight) / 2, sidePanelWidth, drawHeight);
	}

	@Override
	public void render(@NotNull SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (loaded) {
			super.render(graphics, mouseX, mouseY, delta);
			renderLockup(graphics.vanilla());
			graphics.vanilla().vLine(sidePanelWidth, 0, height, 0xFFFFFFFF);
		} else {
			int textWidth = font.width(LOADING_INDICATOR);
			graphics.drawText(font, LOADING_INDICATOR, width - textWidth - 10, height - 15, 0xFFFFFFFF, true);
		}
	}

	@Override
	public void removed() {
		if (!previousSelections.equals(selections)) {
			BallotBoxClient.remainingVotes = categories.stream().mapToInt(VotingCategory::limit).sum() - selections.size();
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
		public static final ResourceLocation VOTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "button_voted");
		public static final ResourceLocation CHECKMARK_TEXTURE = ResourceLocation.fromNamespaceAndPath(BallotBox.ID, "textures/gui/button_checkmark.png");

		public final CategoryContainerWidget parent;
		public boolean selected;
		public boolean prohibited;
		public String url;
		public ResourceLocation texture;

		public VotingOptionButtonWidget(Position position, int width, int height, VotingCategory category, VotingOption option, CategoryContainerWidget parent, boolean prohibited) {
			super(position, width, height, Component.literal(option.name()), button -> {
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
			if (!modIconCache.containsKey(option.id())) {
				modIconCache.put(option.id(), ResourceLocation.fromNamespaceAndPath(BallotBox.ID, option.id().replaceAll("[^a-zA-Z0-9_]", "") + "_icon"));
				Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(option.mod_id().isPresent() ? option.mod_id().get() : option.id())
					.or(() -> FabricLoader.getInstance().getModContainer(option.id().replace("_", "-")))
					.or(() -> FabricLoader.getInstance().getModContainer(option.id().replace("_", "")));
				DynamicTexture icon = mod.isPresent() ? ModMetaUtil.getIcon(mod.get(), 64 * this.client.options.guiScale().get()) : ModMetaUtil.getMissingIcon();
				this.client.getTextureManager().register(modIconCache.get(option.id()), icon);
			}
			texture = modIconCache.get(option.id());
			if (option.platform().type().equals("modrinth")) url = option.platform().project_id().map("https://modrinth.com/mod/%s"::formatted).orElse(null);
			if (option.platform().type().equals("other")) url = option.platform().homepage_url().orElse(null);
			setTooltip(url == null ? Component.literal(option.description()).withStyle(ChatFormatting.GRAY) : Component.literal(option.description()).withStyle(ChatFormatting.GRAY).append(Component.literal("\n")).append(Component.literal("Right-Click").withStyle(ChatFormatting.GOLD)).append(Component.literal(" to open the mod page.").withStyle(ChatFormatting.WHITE)));
		}

		public boolean isActive() {
			return !prohibited && active;
		}

		@Override
		public @NotNull TooltipData getTooltip() {
			return isActive()
				? super.getTooltip()
				: prohibited
				? TooltipData.builder().text(Component.literal("Prohibited by another category!").withStyle(ChatFormatting.GRAY)).build()
				: TooltipData.builder().text(Component.literal("You've reached the category vote limit!").withStyle(ChatFormatting.GRAY)).build();
		}

		@Override
		protected boolean onMouseClick(double mouseX, double mouseY, int button) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_2 && url != null) {
				Util.getPlatform().openUri(url); // confirmation screen causes save
				return true;
			}
			return super.onMouseClick(mouseX, mouseY, button);
		}

		@Override
		protected ResourceLocation getTexture() {
			if (selected) {
				return VOTED_TEXTURE;
			}
			return super.getTexture();
		}

		@Override
		protected void renderButton(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
			int textWidth = client.font.width(getMessage());
			int left = getX() + 2, right = getX() + getWidth() - 2;
			int bottom = getY() + getHeight();
			int textY = (getY() * 2 + getHeight() - 9) / 2 + 1;
			if (texture != null) {
				graphics.vanilla().blit(RenderPipelines.GUI_TEXTURED, texture, left, getY() + 2, 0, 0, 16, 16, 16, 16, 16, 16);
			}
			if (textWidth <= getWidth()) {
				graphics.vanilla().drawCenteredString(client.font, getMessage(), left + getWidth() / 2, textY, 0xFFFFFFFF);
				return;
			}
			int extraWidth = textWidth - getWidth();
			double seconds = (double) Util.getMillis() / 1000.0;
			double clampedWidth = Math.max(extraWidth * 0.5, 3.0);
			double scroll = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2) * seconds / clampedWidth)) / 2.0 + 0.5;
			double offset = Mth.lerp(scroll, 0.0, extraWidth);
			graphics.enableScissor(left, Math.max(getY(), parent.getY()), right, bottom);
			graphics.vanilla().drawString(client.font, getMessage(), left - (int) offset, textY, 0xFFFFFFFF);
			graphics.disableScissor();
		}

		@Override
		protected void renderWidget(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
			super.renderWidget(graphics, mouseX, mouseY, delta);
			if (selected) {
				graphics.drawTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_TEXTURE, getX() + getWidth() - 11, getY() + getHeight() - 9, 0, 0, 7, 6, 7, 6);
			}
		}

		@Override
		public void render(@NotNull SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.enableScissor(parent.getX(), parent.getY(), parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight());
			super.render(graphics, mouseX, mouseY, delta);
			graphics.disableScissor();
		}
	}

	public class CategoryContainerWidget extends SpruceContainerWidget {
		public final VotingCategory category;

		public Map<String, VotingOptionButtonWidget> buttons = new ConcurrentHashMap<>();
		public Component titleText;

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
				options.sort(Comparator.comparing(o -> o.name().toLowerCase()));
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
			titleText = Component.literal(category.name()).append(Component.literal(" (" + selected + "/" + category.limit() + ")").withStyle(atLimit ? ChatFormatting.GREEN : ChatFormatting.GRAY));
			buttons.forEach((id, button) -> button.setActive(button.selected || !atLimit));
		}

		public void updateProhibitions(String optionId, boolean selected) {
			category.prohibitions().ifPresent(prohibitions -> {
				for (var prohibition : prohibitions) {
					categoryWidgets.get(prohibition).buttons.get(optionId).prohibited = selected;
				}
			});
		}

		public float drawTitleText(GuiGraphics context) {
			int titleWidth = client.font.width(titleText);
			float titleScale = Math.min((float) (width - 20) / titleWidth, 2.0f);
			context.pose().pushMatrix();
			context.pose().translate(getPosition().getX() + 10, 10);
			context.pose().scale(titleScale, titleScale);
			context.drawString(client.font, titleText, 0, 0, 0xFFFFFFFF, true);
			context.pose().popMatrix();
			return titleScale;
		}

		@Override
		protected void renderWidget(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
			float titleScale = drawTitleText(graphics.vanilla());
			graphics.drawText(client.font, Component.literal(category.description()), getPosition().getX() + 10, 15 + (int) (9 * titleScale), 0xFFFFFFFF, true);
			super.renderWidget(graphics, mouseX, mouseY, delta);
		}
	}
}
