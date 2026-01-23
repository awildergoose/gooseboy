package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

import static awildgoose.gooseboy.screen.renderer.CrateRendererScreen.SCREEN_UI_LOCATION;

public class MiniView implements AutoCloseable {
	public final int fbWidth;
	public final int fbHeight;
	public GooseboyPainter painter;

	public MiniView(GooseboyCrate crate) {
		painter = new GooseboyPainter(crate);
		fbWidth = crate.fbWidth;
		fbHeight = crate.fbHeight;
	}

	public void init() {
		this.painter.initDrawing();
	}

	public void render(GuiGraphics guiGraphics, CrateLayout layout) {
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				layout.bgX, layout.bgY,
				0, 0,
				layout.bgWidth, layout.bgHeight,
				layout.bgWidth, layout.bgHeight
		);
		this.painter.render(guiGraphics, layout.fbX, layout.fbY, layout.fbDestWidth, layout.fbDestHeight);
	}

	@Override
	public void close() {
		this.painter.close();
	}
}
