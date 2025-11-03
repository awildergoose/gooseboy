package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.Gooseboy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WasmSelectionList extends ObjectSelectionList<WasmSelectionList.Entry> {
	public WasmSelectionList(Minecraft minecraft, int i, int j, int k, int l) {
		super(minecraft, i, j, k, l);

		for (int t = 0; t < 10; t++) {
			this.addEntry(new Entry(minecraft, this, "guh"));
			this.addEntry(new Entry(minecraft, this, "buh"));
			this.addEntry(new Entry(minecraft, this, "luh"));
			this.addEntry(new Entry(minecraft, this, "nuh"));
		}
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		return super.keyPressed(keyEvent);
	}

	public static class Entry extends ObjectSelectionList.Entry<WasmSelectionList.Entry> {
		private static final ResourceLocation WASM_ICON = ResourceLocation.fromNamespaceAndPath(
				Gooseboy.MOD_ID, "textures/gui/wasm_icon.png");
		private final StringWidget text;
		private final ImageButton runButton;
		private final ImageButton settingsButton;

		public Entry(Minecraft minecraft, WasmSelectionList list, String text) {
			int i = list.getRowWidth() - this.getTextX() - 2;
			Component component = Component.literal(text);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);

			this.runButton = new ImageButton(0, 0, 14, 14, new WidgetSprites(
					ResourceLocation.withDefaultNamespace("widget/checkbox_selected"),
					ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted")
			), (b) -> {
				// run
			});

			this.settingsButton = new ImageButton(0, 0, 15, 15, new WidgetSprites(
					ResourceLocation.withDefaultNamespace("widget/locked_button"),
					ResourceLocation.withDefaultNamespace("widget/locked_button_highlighted")
			), (b) -> {
				// settings
			});
		}

		@Override
		public int getHeight() {
			return 16 + 1;
		}

		@Override
		public int getContentHeight() {
			return super.getContentHeight();
		}

		private int getTextX() {
			return this.getContentX() + 16 + 4;
		}

		@Override
		public @NotNull Component getNarration() {
			return this.text.getMessage();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			int contentX = this.getContentX();
			int contentY = this.getContentY() + 1;
			int centeredY = contentY - 2;
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WASM_ICON, contentX, centeredY, 0, 0, 15, 15, 15, 15);
			int k = this.getTextX();
			this.text.setPosition(k, contentY);
			this.text.render(guiGraphics, i, j, f);

			// the lock icon
			this.settingsButton.setPosition(getContentRight() - 16, centeredY);
			this.settingsButton.render(guiGraphics, i, j, f);

			// the checkbox icon
			this.runButton.setPosition(getContentRight() - 16 - 16 - 2, centeredY);
			this.runButton.render(guiGraphics, i, j, f);
		}
	}
}
