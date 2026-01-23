package awildgoose.gooseboy.screen.widgets;

import awildgoose.gooseboy.*;
import awildgoose.gooseboy.crate.CrateLoader;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.CrateSettingsScreen;
import awildgoose.gooseboy.screen.renderer.CenteredCrateScreen;
import com.dylibso.chicory.wasm.UninstantiableException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CrateSelectionList extends ObjectSelectionList<CrateSelectionList.Entry> {
	private final Screen parent;
	public Sort sort;

	public CrateSelectionList(Screen parent, Minecraft minecraft, int i, int j, int k, int l, Sort sort) {
		super(minecraft, i, j, k, l);
		this.parent = parent;
		this.sort = sort;
		this.rebuildEntries(true);
	}

	public void rebuildEntries(boolean allCrates) {
		this.clearEntries();
		List<PathOrCrate> crates = Wasm.listWasmCrates()
				.stream()
				.map(f -> new PathOrCrate(f, null))
				.collect(Collectors.toList());
		if (!allCrates) {
			crates = Gooseboy.getCrates()
					.values()
					.stream()
					.map(gooseboyCrateCrateMetaPair -> new PathOrCrate(null, gooseboyCrateCrateMetaPair.getLeft()))
					.collect(Collectors.toList());
		}

		switch (sort) {
			case FILENAME -> crates.sort(Comparator.comparing(PathOrCrate::getFileName));
			case LAST_MODIFIED -> crates.sort(Comparator.comparingLong(p -> {
						if (!((PathOrCrate) p).hasPath())
							return Long.MIN_VALUE;
						try {
							return Files.getLastModifiedTime(((PathOrCrate) p).path)
									.toMillis();
						} catch (IOException e) {
							return Long.MIN_VALUE;
						}
					})
													  .reversed());
		}

		crates.forEach(f -> this.addEntry(new Entry(parent, minecraft, this, f)));
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	public enum Sort {
		FILENAME,
		LAST_MODIFIED
	}

	record PathOrCrate(Path path, GooseboyCrate crate) {
		public String getFileName() {
			return this.hasCrate() ? crate.name : this.path.getFileName()
					.toString();
		}

		public boolean hasPath() {
			return this.path != null;
		}

		public boolean hasCrate() {
			return this.crate != null;
		}
	}

	public static class Entry extends ObjectSelectionList.Entry<CrateSelectionList.Entry> {
		private static final ResourceLocation WASM_ICON = Gooseboy.withLocation("textures/gui/wasm_icon.png");
		private final StringWidget text;
		private final ImageButton settingsOrStopButton;
		private final ImageButton runButton;

		public Entry(Screen parent, Minecraft minecraft, CrateSelectionList list, PathOrCrate poc) {
			int i = list.getRowWidth() - this.getTextX() - 2;
			String text = poc.getFileName();
			Component component = Component.literal(text);
			this.text = new StringWidget(component, minecraft.font);
			this.text.setMaxWidth(i);

			this.runButton = new ImageButton(0, 0, 15, 15, new WidgetSprites(
					poc.hasCrate() ?
							Gooseboy.withLocation("widget/close_button")
							: Gooseboy.withLocation("widget/run_button"),
					poc.hasCrate() ?
							Gooseboy.withLocation("widget/close_button_highlighted")
							: Gooseboy.withLocation("widget/run_button_highlighted")
			), (b) -> {
				if (poc.hasCrate()) {
					// stop
					if (GooseboyClient.miniviewsByInstance.containsKey(poc.crate.instance)) {
						GooseboyClient.miniviewsByInstance.get(poc.crate.instance)
								.close();
					} else {
						Gooseboy.LOGGER.error("unable to stop crate, miniview renderer is not present?");
					}

					list.rebuildEntries(false);
					return;
				}

				// run
				WasmInputManager.reset();

				try {
					GooseboyCrate crate = CrateLoader.makeCrate(poc.path);

					if (crate.isOk) {
						if (!crate.isMiniView)
							minecraft.setScreen(new CenteredCrateScreen(crate));
						else
							minecraft.setScreen(null);
					} else {
						throw new RuntimeException("Crate aborted upon startup");
					}
				} catch (Exception e) {
					e.printStackTrace();

					if (e instanceof UninstantiableException) {
						Gooseboy.ccb.doTranslatedErrorMessage(
								"ui.gooseboy.not_enough_memory.title",
								"ui.gooseboy.not_enough_memory.body");
					} else if (e instanceof CrateLoader.CrateLoaderException er) {
						Gooseboy.ccb.doErrorMessage(er.title, er.body);
					} else {
						Gooseboy.ccb.doTranslatedErrorMessage(
								"ui.gooseboy.crate_run_failed.title",
								"ui.gooseboy.crate_run_failed.body");
					}
				}
			});

			this.settingsOrStopButton = new ImageButton(0, 0, 15, 15, new WidgetSprites(
					Gooseboy.withLocation("widget/settings_button"),
					Gooseboy.withLocation("widget/settings_button_highlighted")
			), (b) -> {
				if (poc.hasCrate()) {
					// change layout
					MiniView miniview = GooseboyClient.miniviewsByInstance.get(poc.crate.instance);
					miniview.layoutType = switch (miniview.layoutType) {
						case TOP_RIGHT -> MiniView.LayoutType.BOTTOM_RIGHT;
						case TOP_LEFT -> MiniView.LayoutType.CENTERED;
						case BOTTOM_RIGHT -> MiniView.LayoutType.BOTTOM_LEFT;
						case BOTTOM_LEFT -> MiniView.LayoutType.TOP_LEFT;
						case CENTERED -> MiniView.LayoutType.TOP_RIGHT;
					};

					return;
				}

				// settings
				minecraft.setScreen(new CrateSettingsScreen(parent, text));
			});
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			if (this.settingsOrStopButton.mouseClicked(mouseButtonEvent, bl)) return true;
			if (this.runButton.mouseClicked(mouseButtonEvent, bl)) return true;
			return super.mouseClicked(mouseButtonEvent, bl);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
			if (this.settingsOrStopButton.mouseReleased(mouseButtonEvent)) return true;
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
			this.settingsOrStopButton.setPosition(getContentRight() - 16, centeredY);
			this.settingsOrStopButton.render(guiGraphics, i, j, f);

			// the checkbox icon
			this.runButton.setPosition(getContentRight() - 16 - 16 - 2, centeredY);
			this.runButton.render(guiGraphics, i, j, f);
		}
	}
}
