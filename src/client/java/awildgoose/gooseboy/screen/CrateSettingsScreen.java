package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.screen.widgets.CrateSettingsList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CrateSettingsScreen extends Screen {
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 20, 20);
	protected final CrateSettingsList list;
	private final String crateName;
	private final Screen parent;

	public CrateSettingsScreen(Screen parent, String crateName) {
		super(Component.literal("%s - Settings".formatted(crateName)));
		this.crateName = crateName;
		this.parent = parent;
		this.list = new CrateSettingsList(Minecraft.getInstance(), 0, 0, 200, 200, crateName);
		this.layout.addToContents(this.list);
	}

	@Override
	protected void init() {
		LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
		header.addChild(new StringWidget(this.title, this.font), LayoutSettings::alignHorizontallyCenter);
		this.layout.addToFooter(
				Button.builder(Component.translatable("gui.ok"), (b) -> this.onClose()).build(),
				(v) -> v.alignHorizontallyCenter().paddingTop(-10));
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void repositionElements() {
		this.list.updateSizeAndPosition(this.width, this.layout.getContentHeight(), this.layout.getHeaderHeight());
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) this.minecraft.setScreen(this.parent);
		ConfigManager.setCratePermissions(this.crateName, this.list.permissions);
	}
}
