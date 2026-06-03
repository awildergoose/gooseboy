package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateStorage;
import awildgoose.gooseboy.crate.GooseboyCrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CrateSettingsList extends ObjectSelectionList<CrateSettingsList.Entry> {
	public final List<GooseboyCrate.Permission> permissions;
	public Pair<Integer, Integer> memoryLimits;
	public int storageSize;
	public int storageUploadOffset;

	public CrateSettingsList(Minecraft minecraft, int i, int j, int k, int l, String crateName, Path goosePath) {
		super(minecraft, i, j, k, l);
		this.permissions = new ArrayList<>(ConfigManager.getEffectivePermissions(crateName));
		this.memoryLimits = ConfigManager.getMemoryLimits(crateName);
		this.storageSize = ConfigManager.getStorageSize(crateName);
		this.storageUploadOffset = 0;
		this.addEntry(new TextEntry(
				minecraft, this, true,
				"ui.gooseboy.settings.allocated",
				formatBytes(CrateStorage.getSizeOf(crateName, goosePath))));
		this.addEntry(new TextEntry(minecraft, this, true, "ui.gooseboy.settings.initial_memory"));
		this.addEntry(new NumberEditEntry(
				minecraft, this, "ui.gooseboy.settings.initial_memory",
				this.memoryLimits.getLeft()
						.toString(), s -> {
			Integer n = parseNumber(s);
			if (n == null) return;
			this.memoryLimits = Pair.of(n, this.memoryLimits.getRight());
		}));
		this.addEntry(new TextEntry(minecraft, this, true, "ui.gooseboy.settings.maximum_memory"));
		this.addEntry(new NumberEditEntry(
				minecraft, this, "ui.gooseboy.settings.maximum_memory",
				this.memoryLimits.getRight()
						.toString(), s -> {
			Integer n = parseNumber(s);
			if (n == null) return;
			this.memoryLimits = Pair.of(this.memoryLimits.getLeft(), n);
		}));
		this.addEntry(new TextEntry(minecraft, this, true, "ui.gooseboy.settings.storage_size"));
		this.addEntry(new NumberEditEntry(
				minecraft, this, "ui.gooseboy.settings.storage_size",
				Integer.toString(this.storageSize / 1024), s -> {
			Integer n = parseNumber(s);
			if (n == null) return;
			this.storageSize = n * 1024;
		}));
		this.addEntry(new ButtonEntry(
				minecraft, this, "ui.gooseboy.settings.dump_storage", () -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer aFilterPatterns = stack.mallocPointer(2);
				aFilterPatterns.put(stack.UTF8("*.gsb"));
				aFilterPatterns.put(stack.UTF8("*.*"));
				aFilterPatterns.flip();

				String file = TinyFileDialogs.tinyfd_saveFileDialog(
						"Select a file",
						"",
						aFilterPatterns,
						"Gooseboy storage files (*.gsb) or any file (*.*)"
				);

				if (file != null) {
					Path path = Path.of(file);
					CrateStorage crateStorage = new CrateStorage(crateName, goosePath);

					if (path.toString()
							.endsWith(".gsb")) {
						try (DataOutputStream w = new DataOutputStream(Files.newOutputStream(
								path, StandardOpenOption.CREATE,
								StandardOpenOption.TRUNCATE_EXISTING))) {
							crateStorage.writeSerialized(w);
						}
					} else {
						Files.write(
								path, crateStorage.readAll()
										.array());
					}
				}
			} catch (IOException e) {
				Gooseboy.ccb.doErrorMessage("Crate dump failed", e.toString());
			}
		}));
		this.addEntry(new TextEntry(minecraft, this, true, "ui.gooseboy.settings.storage_upload_offset"));
		this.addEntry(new NumberEditEntry(
				minecraft, this, "ui.gooseboy.settings.storage_upload_offset",
				Integer.toString(this.storageUploadOffset), s -> {
			Integer n = parseNumber(s);
			if (n == null) return;
			this.storageUploadOffset = n;
		}));
		this.addEntry(new ButtonEntry(
				minecraft, this, "ui.gooseboy.settings.upload_storage", () -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer aFilterPatterns = stack.mallocPointer(2);
				aFilterPatterns.put(stack.UTF8("*.gsb"));
				aFilterPatterns.put(stack.UTF8("*.*"));
				aFilterPatterns.flip();

				String file = TinyFileDialogs.tinyfd_openFileDialog(
						"Select a file",
						"",
						aFilterPatterns,
						"Gooseboy storage files (*.gsb) or any file (*.*)",
						false
				);

				if (file != null) {
					Path path = Path.of(file);
					ByteBuffer data;

					if (file.endsWith(".gsb")) {
						CrateStorage storage = new CrateStorage(path);
						data = storage.readAll();
					} else {
						data = ByteBuffer.wrap(Files.readAllBytes(path));
					}

					ConfigManager.setCrateStorageSize(crateName, this.storageSize);
					CrateStorage toUploadTo = new CrateStorage(crateName, goosePath);
					if (toUploadTo.writeDirect(this.storageUploadOffset, data)) {
						toUploadTo.save();
						Gooseboy.ccb.doTranslatedMessage(
								"ui.gooseboy.storage_upload_successful.title",
								"ui.gooseboy.storage_upload_successful.body");
					} else {
						Gooseboy.ccb.doTranslatedErrorMessage(
								"ui.gooseboy.storage_upload_failed.title",
								"ui.gooseboy.storage_upload_failed.body");
					}
				}
			} catch (IOException e) {
				Gooseboy.ccb.doErrorMessage("File IO failed", e.toString());
			}
		}));
		this.addEntry(new TextEntry(minecraft, this, true, "ui.gooseboy.settings.permissions"));

		for (GooseboyCrate.Permission permission : GooseboyCrate.Permission.values()) {
			String title = "ui.gooseboy.settings.permission.%s".formatted(permission.name());
			this.addEntry(new BooleanEntry(
					minecraft, this, title, this.permissions.contains(permission),
					(checkbox, bl) -> {
						if (bl) {
							this.permissions.add(permission);
						} else {
							this.permissions.remove(permission);
						}
					}));
		}
	}

	private static Integer parseNumber(String s) {
		if (s.isEmpty()) return null;

		try {
			return Integer.decode(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String formatBytes(long bytes) {
		final String[] units = {"bytes", "kilobytes", "megabytes", "gigabytes"};
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

	@Override
	public int getRowWidth() {
		return 270;
	}

	public abstract static class Entry extends ObjectSelectionList.Entry<CrateSettingsList.Entry> {
		public final CrateSettingsList list;

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

		public TextEntry(Minecraft minecraft, CrateSettingsList list, boolean bold, String text, Object... o) {
			super(minecraft, list, text);
			int i = list.getRowWidth() - this.getTextX() - 2;
			Component component = Component.translatable(text, o);
			component = Component.empty()
					.withStyle(Style.EMPTY.withBold(bold))
					.append(component);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);
		}

		@Override
		public boolean shouldTakeFocusAfterInteraction() {
			return false;
		}

		private int getTextX() {
			return this.getContentX() + 4;
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

	public static class ButtonEntry extends Entry {
		private final Button button;
		private final Component text;

		public ButtonEntry(Minecraft minecraft, CrateSettingsList list, String text, Runnable callback) {
			super(minecraft, list, text);
			this.text = Component.translatable(text);
			this.button = new Button.Builder(this.text, button -> callback.run()).build();
		}

		@Override
		public boolean shouldTakeFocusAfterInteraction() {
			return false;
		}

		@Override
		public @NotNull Component getNarration() {
			return this.text;
		}

		@Override
		public int getHeight() {
			return super.getHeight() + 6;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			super.renderContent(guiGraphics, i, j, bl, f);
			this.button.setX(this.getContentX() - 2);
			this.button.setY(this.getContentY());
			this.button.setWidth(this.getContentWidth());
			this.button.render(guiGraphics, i, j, f);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			if (this.button.mouseClicked(mouseButtonEvent, bl)) return true;
			return super.mouseClicked(mouseButtonEvent, bl);
		}

		@Override
		public boolean keyPressed(KeyEvent keyEvent) {
			if (this.button.keyPressed(keyEvent)) return true;
			return super.keyPressed(keyEvent);
		}

		@Override
		public boolean keyReleased(KeyEvent keyEvent) {
			if (this.button.keyReleased(keyEvent)) return true;
			return super.keyReleased(keyEvent);
		}
	}

	public static class BooleanEntry extends Entry {
		private final Checkbox checkbox;

		public BooleanEntry(Minecraft minecraft, CrateSettingsList list, String text, boolean checked,
		                    Checkbox.OnValueChange callback, Object... o) {
			super(minecraft, list, text);
			this.checkbox =
					Checkbox.builder(Component.translatable(text, o), minecraft.font)
							.selected(checked)
							.onValueChange(callback)
							.build();
		}

		@Override
		public @NotNull Component getNarration() {
			return this.checkbox.getMessage();
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
		public boolean keyPressed(KeyEvent keyEvent) {
			if (this.checkbox.keyPressed(keyEvent)) return true;
			return super.keyPressed(keyEvent);
		}

		@Override
		public boolean keyReleased(KeyEvent keyEvent) {
			if (this.checkbox.keyReleased(keyEvent)) return true;
			return super.keyReleased(keyEvent);
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			super.renderContent(guiGraphics, i, j, bl, f);
			int contentX = this.getContentX();
			int contentY = this.getContentY() + 1;
			int centeredY = contentY - 2;

			this.checkbox.setPosition(contentX, centeredY);
			this.checkbox.render(guiGraphics, i, j, f);
		}
	}

	public static class NumberEditEntry extends Entry {
		private final EditBox editBox;

		public NumberEditEntry(Minecraft minecraft, CrateSettingsList list, String text, String defaultText,
		                       Consumer<String> responder, Object... o) {
			super(minecraft, list, text);
			this.editBox = new EditBox(minecraft.font, 0, 0, Component.translatable(text, o));
			this.editBox.setValue(defaultText);
			this.editBox.setFilter(p -> {
				if (p.isEmpty()) return true;
				if (p.length() > 2) {
					return parseNumber(p) != null;
				}

				if (p.length() == 1) {
					char c = p.charAt(0);
					return c == '+' || c == '-' || Character.isDigit(c);
				}

				char first = p.charAt(0), second = p.charAt(1);
				if (Character.isDigit(second)) return true;
				return (first == '0' && (second == 'x' || second == 'X'));
			});
			this.editBox.setResponder(responder);
			this.editBox.setCursorPosition(0);
			this.editBox.setHighlightPos(0);
			this.editBox.setFocused(true);
		}

		@Override
		public @NotNull Component getNarration() {
			return this.editBox.getMessage();
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

			this.editBox.setWidth(this.getWidth());
			this.editBox.setHeight(this.getHeight());
			this.editBox.setPosition(contentX, centeredY);
			this.editBox.render(guiGraphics, i, j, f);
		}
	}
}
