package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.Wasm;
import awildgoose.gooseboy.WasmInputManager;
import awildgoose.gooseboy.crate.CrateLoader;
import awildgoose.gooseboy.screen.CenteredCrateScreen;
import awildgoose.gooseboy.screen.CrateSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CrateSelectionList extends ObjectSelectionList<CrateSelectionList.Entry> {
	public CrateSelectionList(Screen parent, Minecraft minecraft, int i, int j, int k, int l) {
		super(minecraft, i, j, k, l);
		Wasm.listWasmCrates().forEach(f -> this.addEntry(new Entry(parent, minecraft, this, f)));
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		return super.keyPressed(keyEvent);
	}

	public static class Entry extends ObjectSelectionList.Entry<CrateSelectionList.Entry> {
		private static final ResourceLocation WASM_ICON = ResourceLocation.fromNamespaceAndPath(
				Gooseboy.MOD_ID, "textures/gui/wasm_icon.png");
		private final StringWidget text;
		private final ImageButton runButton;
		private final ImageButton settingsButton;

		public Entry(Screen parent, Minecraft minecraft, CrateSelectionList list, String text) {
			int i = list.getRowWidth() - this.getTextX() - 2;
			Component component = Component.literal(text);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);

			this.runButton = new ImageButton(0, 0, 15, 15, new WidgetSprites(
					ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "widget/run_button"),
					ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "widget/run_button_highlighted")
			), (b) -> {
				// run
				WasmInputManager.reset();
				try {
					var crate = CrateLoader.makeCrate(text);

					if (crate.isOk) {
						minecraft.setScreen(new CenteredCrateScreen(crate));
					} else {
						throw new RuntimeException("Crate aborted upon startup");
					}
				} catch (Exception e) {
					Gooseboy.ccb.doErrorMessage("Failed to load crate", "Check the console for more information.");
					e.printStackTrace();
				}
			});

			this.settingsButton = new ImageButton(0, 0, 15, 15, new WidgetSprites(
					ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "widget/settings_button"),
					ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "widget/settings_button_highlighted")
			), (b) -> {
				// settings
				minecraft.setScreen(new CrateSettingsScreen(parent, text));
			});
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			if (this.settingsButton.mouseClicked(mouseButtonEvent, bl)) return true;
			if (this.runButton.mouseClicked(mouseButtonEvent, bl)) return true;
			return super.mouseClicked(mouseButtonEvent, bl);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
			if (this.settingsButton.mouseReleased(mouseButtonEvent)) return true;
			if (this.runButton.mouseReleased(mouseButtonEvent)) return true;
			return super.mouseReleased(mouseButtonEvent);
		}

		@Override
		public int getHeight() {
			return 16 + 1;
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
