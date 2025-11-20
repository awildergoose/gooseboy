package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateLoader;
import awildgoose.gooseboy.screen.widgets.CrateSelectionList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static awildgoose.gooseboy.Wasm.isValidGooseboyFilename;

public class CrateListScreen extends Screen {
	protected ObjectSelectionList<?> list;
	private HeaderAndFooterLayout layout;

	public CrateListScreen() {
		super(Component.literal("Gooseboy"));
		this.reloadList(false);
	}

	@Override
	public void onFilesDrop(List<Path> list) {
		// upload crate(s)
		list.forEach(path -> {
			if (!isValidGooseboyFilename(path.toString())) {
				Gooseboy.ccb.doTranslatedErrorMessage("ui.gooseboy.upload_failed.title", "ui.gooseboy.upload_failed" +
						".bad_filename", path.getFileName()
															  .toString());
				return;
			}

			try {
				Files.copy(path, CrateLoader.getGooseboyCratesPath()
						.resolve(path.getFileName()));
			} catch (Exception e) {
				e.printStackTrace();
				Gooseboy.ccb.doTranslatedErrorMessage("ui.gooseboy.upload_failed.title", "ui.gooseboy.upload_failed" +
						".body", path.getFileName()
															  .toString());
			}
		});

		this.reloadList(true);
	}

	public void reloadList(boolean rebuild) {
		this.layout = new HeaderAndFooterLayout(this, 20, 20);
		this.list = new CrateSelectionList(
				this, Minecraft.getInstance(), 0, 0, 200, 200, CrateSelectionList.Sort.FILENAME);
		this.layout.addToContents(this.list);
		if (rebuild)
			this.rebuildWidgets();
	}

	@Override
	protected void init() {
		LinearLayout header = this.layout.addToHeader(LinearLayout.horizontal()
															  .spacing(4));
		header.addChild(new StringWidget(this.title, this.font), (l) -> l.alignHorizontallyCenter()
				.alignVerticallyMiddle());

		LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal()
															  .spacing(4));
		footer.addChild(Button.builder(
						Component.translatable("ui.gooseboy.open_crates_folder"),
						(b) -> Util.getPlatform()
								.openPath(Gooseboy.getGooseboyDirectory()
												  .resolve(
														  "crates")))
								.build(), (v) -> v.alignHorizontallyCenter()
				.paddingTop(-5));
		footer.addChild(Button.builder(
						Component.translatable("ui.gooseboy.refresh"),
						(b) -> this.reloadList(true))
								.build(), (v) -> v.alignHorizontallyCenter()
				.paddingTop(-5));
		footer.addChild(
				Button.builder(Component.translatable("gui.ok"), (b) -> this.onClose())
						.build(),
				(v) -> v.alignHorizontallyCenter()
						.paddingTop(-5));
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
