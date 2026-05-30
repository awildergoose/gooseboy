package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import awildgoose.gooseboy.screen.renderer.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

import static awildgoose.gooseboy.screen.renderer.CrateRendererScreen.SCREEN_UI_LOCATION;

public class MiniView implements AutoCloseable {
	public final int fbWidth;
	public final int fbHeight;
	public LayoutType layoutType;
	public final GooseboyPainter painter;

	public MiniView(GooseboyCrate crate) {
		this.painter = new GooseboyPainter(crate);
		this.fbWidth = crate.fbWidth;
		this.fbHeight = crate.fbHeight;
		this.layoutType = LayoutType.TOP_RIGHT;
	}

	public void render(GuiGraphics guiGraphics) {
		CrateLayout layout = this.layoutType.supplier.apply(
				guiGraphics.guiWidth(), guiGraphics.guiHeight(), this.fbWidth,
				this.fbHeight);
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				layout.bgX, layout.bgY,
				0, 0,
				layout.bgWidth, layout.bgHeight,
				layout.bgWidth, layout.bgHeight
		);
		this.painter.render(guiGraphics, layout.fbX, layout.fbY, layout.fbDestWidth, layout.fbDestHeight);
	}

	public void init() {
		this.painter.initDrawing();
	}

	@Override
	public void close() {
		this.painter.close();
	}

	public enum LayoutType {
		TOP_RIGHT(TopRightCrateScreen.Layout::forSize),
		TOP_LEFT(TopLeftCrateScreen.Layout::forSize),
		BOTTOM_RIGHT(BottomRightCrateScreen.Layout::forSize),
		BOTTOM_LEFT(BottomLeftCrateScreen.Layout::forSize),
		CENTERED(CenteredCrateScreen.Layout::forSize);

		final CrateRendererScreen.LayoutSupplier<?> supplier;

		LayoutType(CrateRendererScreen.LayoutSupplier<?> supplier) {
			this.supplier = supplier;
		}
	}
}
