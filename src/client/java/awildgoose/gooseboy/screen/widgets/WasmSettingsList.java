package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.crate.WasmCrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WasmSettingsList extends ObjectSelectionList<WasmSettingsList.Entry> {
	public List<WasmCrate.Permission> permissions;

	public WasmSettingsList(Minecraft minecraft, int i, int j, int k, int l, String crateName) {
		super(minecraft, i, j, k, l);
		this.permissions = ConfigManager.getEffectivePermissions(crateName);
		this.addEntry(new TextEntry(minecraft, this, "Permissions"));
		for (WasmCrate.Permission permission : WasmCrate.Permission.values()) {
			String name = permission.name().replace("_", " ");
			String title = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
			this.addEntry(new BooleanEntry(minecraft, this, title, permissions.contains(permission)));
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

	public abstract static class Entry extends ObjectSelectionList.Entry<WasmSettingsList.Entry> {
		public Entry(Minecraft ignoredMinecraft, WasmSettingsList ignoredList, String ignoredText) {}

		@Override
		public int getHeight() {
			return 16 + 2;
		}

		@Override
		public @NotNull Component getNarration() {
			return Component.empty();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {}
	}

	public static class TextEntry extends Entry {
		private final StringWidget text;

		public TextEntry(Minecraft minecraft, WasmSettingsList list, String text) {
			super(minecraft, list, text);
			int i = list.getRowWidth() - this.getTextX() - 2;
			Component component = Component.literal(text);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);
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
			super.renderContent(guiGraphics, i, j, bl, f);
			int contentY = this.getContentY() + 1;
			int k = this.getTextX();
			this.text.setPosition(k, contentY);
			this.text.render(guiGraphics, i, j, f);
		}
	}

	public static class BooleanEntry extends Entry {
		private final Checkbox checkbox;

		public BooleanEntry(Minecraft minecraft, WasmSettingsList list, String text, boolean checked) {
			super(minecraft, list, text);
			checkbox = Checkbox.builder(Component.literal(text), minecraft.font).selected(checked).build();
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			if (this.checkbox.mouseClicked(mouseButtonEvent, bl)) return true;
			return super.mouseClicked(mouseButtonEvent, bl);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
			if (this.checkbox.mouseReleased(mouseButtonEvent)) return true;
			return super.mouseReleased(mouseButtonEvent);
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			super.renderContent(guiGraphics, i, j, bl, f);
			int contentX = this.getContentX();
			int contentY = this.getContentY() + 1;
			int centeredY = contentY - 2;

			checkbox.setPosition(contentX, centeredY);
			checkbox.render(guiGraphics, i, j, f);
		}
	}
}
