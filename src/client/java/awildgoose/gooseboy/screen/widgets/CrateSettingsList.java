package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.crate.CrateStorage;
import awildgoose.gooseboy.crate.GooseboyCrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CrateSettingsList extends ObjectSelectionList<CrateSettingsList.Entry> {
	public List<GooseboyCrate.Permission> permissions;
	public Pair<Integer, Integer> memoryLimits;

	public static String formatBytes(long bytes) {
		if (bytes < 1024) return bytes + " B";

		final String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
		int unitIndex = 0;
		double value = bytes;

		while (value >= 1024 && unitIndex < units.length - 1) {
			value /= 1024;
			unitIndex++;
		}

		if (value % 1 == 0) {
			return String.format("%.0f %s", value, units[unitIndex]);
		} else {
			return String.format("%.1f %s", value, units[unitIndex]);
		}
	}

	public CrateSettingsList(Minecraft minecraft, int i, int j, int k, int l, String crateName) {
		super(minecraft, i, j, k, l);
		this.permissions = new ArrayList<>(ConfigManager.getEffectivePermissions(crateName));
		this.memoryLimits = ConfigManager.getMemoryLimits(crateName);
		this.addEntry(new TextEntry(minecraft, this,
									"Allocated storage: %s".formatted(formatBytes(CrateStorage.getSizeOf(crateName)))));
		this.addEntry(new TextEntry(minecraft, this, "Initial memory in KBs"));
		this.addEntry(new NumberEditEntry(minecraft, this, "Initial memory in kilobytes",
										  memoryLimits.getLeft().toString(), s -> {
			if (s.chars().noneMatch(Character::isDigit)) return;
			var n = Integer.parseInt(s);
			this.memoryLimits = Pair.of(n, this.memoryLimits.getRight());
		}));
		this.addEntry(new TextEntry(minecraft, this, "Maximum memory in KBs"));
		this.addEntry(new NumberEditEntry(minecraft, this, "Max memory in kilobytes",
										  memoryLimits.getRight().toString(), s -> {
			if (s.chars().noneMatch(Character::isDigit)) return;
			var n = Integer.parseInt(s);
			this.memoryLimits = Pair.of(this.memoryLimits.getLeft(), n);
		}));
		this.addEntry(new TextEntry(minecraft, this, "Permissions"));
		for (GooseboyCrate.Permission permission : GooseboyCrate.Permission.values()) {
			String name = permission.name()
					.replace("_", " ");
			String title = Character.toUpperCase(name.charAt(0)) + name.substring(1)
					.toLowerCase();
			this.addEntry(new BooleanEntry(minecraft, this, title, permissions.contains(permission),
										   (checkbox, bl) -> {
											   if (bl) {
												   this.permissions.add(permission);
											   } else {
												   this.permissions.remove(permission);
											   }
										   }));
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

	public abstract static class Entry extends ObjectSelectionList.Entry<CrateSettingsList.Entry> {
		public CrateSettingsList list;

		public Entry(Minecraft ignoredMinecraft, CrateSettingsList list, String ignoredText) {
			this.list = list;
		}

		@Override
		public int getHeight() {
			return 16 + 2;
		}

		@Override
		public @NotNull Component getNarration() {
			return Component.empty();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
		}
	}

	public static class TextEntry extends Entry {
		private final StringWidget text;

		public TextEntry(Minecraft minecraft, CrateSettingsList list, String text) {
			super(minecraft, list, text);
			int i = list.getRowWidth() - this.getTextX() - 2;
			Component component = Component.literal(text);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);
		}

		@Override
		public boolean shouldTakeFocusAfterInteraction() {
			return false;
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
			int contentY = this.getContentY() + 3;
			int k = this.getTextX();
			this.text.setPosition(k, contentY);
			this.text.render(guiGraphics, i, j, f);
		}
	}

	public static class BooleanEntry extends Entry {
		private final Checkbox checkbox;

		public BooleanEntry(Minecraft minecraft, CrateSettingsList list, String text, boolean checked,
							Checkbox.OnValueChange callback) {
			super(minecraft, list, text);
			checkbox =
					Checkbox.builder(Component.literal(text), minecraft.font)
							.selected(checked)
							.onValueChange(callback)
							.build();
		}

		@Override
		public @NotNull Component getNarration() {
			return checkbox.getMessage();
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

	public static class NumberEditEntry extends Entry {
		private final EditBox editBox;

		public NumberEditEntry(Minecraft minecraft, CrateSettingsList list, String text, String defaultText,
							   Consumer<String> responder) {
			super(minecraft, list, text);
			editBox = new EditBox(minecraft.font, 0, 0, Component.literal(text));
			editBox.setValue(defaultText);
			editBox.setFilter(p -> p.chars().allMatch(Character::isDigit));
			editBox.setResponder(responder);
			editBox.setCursorPosition(0);
			editBox.setHighlightPos(0);
			editBox.setFocused(true);
		}

		@Override
		public @NotNull Component getNarration() {
			return editBox.getMessage();
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			if (this.editBox.mouseClicked(mouseButtonEvent, bl)) return true;
			return super.mouseClicked(mouseButtonEvent, bl);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
			if (this.editBox.mouseReleased(mouseButtonEvent)) return true;
			return super.mouseReleased(mouseButtonEvent);
		}

		@Override
		public boolean keyPressed(KeyEvent keyEvent) {
			if (this.editBox.keyPressed(keyEvent)) return true;
			return super.keyPressed(keyEvent);
		}

		@Override
		public boolean keyReleased(KeyEvent keyEvent) {
			if (this.editBox.keyReleased(keyEvent)) return true;
			return super.keyReleased(keyEvent);
		}

		@Override
		public boolean charTyped(CharacterEvent characterEvent) {
			if (this.editBox.charTyped(characterEvent)) return true;
			return super.charTyped(characterEvent);
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			super.renderContent(guiGraphics, i, j, bl, f);
			int contentX = this.getContentX() - 2;
			int contentY = this.getContentY();
			int centeredY = contentY - 2;

			editBox.setWidth(this.getWidth());
			editBox.setHeight(this.getHeight());
			editBox.setPosition(contentX, centeredY);
			editBox.render(guiGraphics, i, j, f);
		}
	}
}
