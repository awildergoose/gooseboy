package awildgoose.gooseboy.screen.renderer;

import awildgoose.gooseboy.GooseboyPainter;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class CrateRendererScreen<L extends CrateLayout> extends Screen {
	public final GooseboyPainter painter;
	public final int fbWidth;
	public final int fbHeight;

	public CrateRendererScreen(GooseboyCrate crate, Component component) {
		super(component);
		this.painter = new GooseboyPainter(crate);
		this.fbWidth = crate.fbWidth;
		this.fbHeight = crate.fbHeight;
	}

	@Override
	protected void init() {
		this.painter.initDrawing(fbWidth, fbHeight);
	}

	@Override
	public void onClose() {
		this.painter.close();
		super.onClose();
	}

	public void renderCrate(GuiGraphics guiGraphics, CrateLayout layout) {
		this.painter.render(guiGraphics, layout.fbX, layout.fbY, layout.fbDestWidth, layout.fbDestHeight);
	}

	public abstract L getLayout();
}
