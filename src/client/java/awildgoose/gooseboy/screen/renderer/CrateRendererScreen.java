package awildgoose.gooseboy.screen.renderer;

import awildgoose.gooseboy.GooseboyPainter;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class CrateRendererScreen extends Screen {
	public final GooseboyPainter painter;

	public CrateRendererScreen(GooseboyCrate crate, Component component) {
		super(component);
		this.painter = new GooseboyPainter(crate);
	}

	@Override
	protected void init() {
		this.painter.initDrawing();
	}

	@Override
	public void onClose() {
		this.painter.close();
		super.onClose();
	}

	public void renderCrate(GuiGraphics guiGraphics, CrateLayout layout) {
		this.painter.render(guiGraphics, layout.fbX, layout.fbY, layout.fbDestWidth, layout.fbDestHeight);
	}
}
