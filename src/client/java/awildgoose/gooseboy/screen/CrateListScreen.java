package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.screen.widgets.CrateSelectionList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static awildgoose.gooseboy.Wasm.isValidGooseboyFilename;

public class CrateListScreen extends Screen {
	protected CrateSelectionList list;
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
				Gooseboy.ccb.doTranslatedErrorMessage(
						"ui.gooseboy.upload_failed.title", "ui.gooseboy.upload_failed" +
								".bad_filename", path.getFileName()
								.toString());
				return;
			}

			try {
				Files.copy(
						path, Gooseboy.getGooseboyCratesDirectory()
								.resolve(path.getFileName()));
			} catch (Exception e) {
				e.printStackTrace();
				Gooseboy.ccb.doTranslatedErrorMessage(
						"ui.gooseboy.upload_failed.title", "ui.gooseboy.upload_failed" +
								".body", path.getFileName()
								.toString());
			}
		});

		this.reloadList(true);
	}

	public void reloadList(boolean rebuild) {
		CrateSelectionList.Sort sort = CrateSelectionList.Sort.LAST_MODIFIED;
		if (this.list != null)
			sort = this.list.sort;
		this.layout = new HeaderAndFooterLayout(this, 30, 30);
		this.list = new CrateSelectionList(
				this, Minecraft.getInstance(), 0, 0, 200, 200, sort);
		this.layout.addToContents(this.list);
		if (rebuild)
			this.rebuildWidgets();
	}

	@Override
	protected void init() {
		LinearLayout header = this.layout.addToHeader(LinearLayout.horizontal()
															  .spacing(4));

		header.addChild(
				Button.builder(Component.translatable("ui.gooseboy.all_crates"), (b) -> list.rebuildEntries(true))
						.build(), LayoutSettings::alignHorizontallyLeft);
		header.addChild(
				Button.builder(Component.translatable("ui.gooseboy.running_crates"), (b) -> list.rebuildEntries(false))
						.build(), LayoutSettings::alignHorizontallyRight);

		LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal()
															  .spacing(4));
		footer.addChild(
				Button.builder(
								Component.translatable("ui.gooseboy.open_crates_folder"),
								(b) -> Util.getPlatform()
										.openPath(Gooseboy.getGooseboyCratesDirectory()))
						.build(), (v) -> v.alignHorizontallyCenter()
						.alignVerticallyMiddle());
		footer.addChild(
				Button.builder(
								Component.translatable("ui.gooseboy.refresh"),
								(b) -> this.reloadList(true))
						.build(), (v) -> v.alignHorizontallyCenter()
						.alignVerticallyMiddle());
		footer.addChild(
				Button.builder(
								Component.translatable("ui.gooseboy.sort." + list.sort.name()),
								(b) -> {
									list.sort = list.sort == CrateSelectionList.Sort.FILENAME ?
											CrateSelectionList.Sort.LAST_MODIFIED : CrateSelectionList.Sort.FILENAME;
									this.reloadList(true);
								})
						.build(), (v) -> v.alignHorizontallyCenter()
						.alignVerticallyMiddle());
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
