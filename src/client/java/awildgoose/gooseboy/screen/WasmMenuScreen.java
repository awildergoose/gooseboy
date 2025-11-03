package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.screen.widgets.WasmSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WasmMenuScreen extends Screen {
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 20, 20);
	protected final ObjectSelectionList<?> list;

	public WasmMenuScreen() {
		super(Component.literal("WASM"));
		this.list = new WasmSelectionList(Minecraft.getInstance(), 0, 0, 200, 200);
		this.layout.addToContents(this.list);
	}

	@Override
	protected void init() {
		LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
		header.addChild(new StringWidget(this.title, this.font), LayoutSettings::alignHorizontallyCenter);

		if (false) {
			GridLayout grid = new GridLayout();
			grid.defaultCellSetting()
					.paddingHorizontal(2)
					.paddingBottom(2)
					.alignVerticallyMiddle()
					.alignHorizontallyCenter();

			GridLayout.RowHelper rowHelper = grid.createRowHelper(1);

			for (int i = 1; i <= 10; i++) {
				int index = i;
				rowHelper.addChild(Button.builder(Component.literal("Button " + i),
												  b -> Gooseboy.LOGGER.info("Clicked button %d".formatted(index))).width(150).build());
			}

			this.layout.addToContents(grid);
			this.layout.addToFooter(Button.builder(Component.literal("Done"), b -> this.onClose()).width(200).build(),
									(f) -> f.paddingTop(-10));
		}
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
}
