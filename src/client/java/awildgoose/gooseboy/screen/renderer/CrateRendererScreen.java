package awildgoose.gooseboy.screen.renderer;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.GooseboyPainter;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class CrateRendererScreen<L extends CrateLayout> extends Screen {
	public static final int IMAGE_WIDTH = 330;
	public static final int IMAGE_HEIGHT = 214;
	public static final ResourceLocation SCREEN_UI_LOCATION = Gooseboy.withLocation("textures/gui/wasm.png");

	public final GooseboyPainter painter;
	public final int fbWidth;
	public final int fbHeight;
	public final boolean allowsMovement;

	public CrateRendererScreen(GooseboyCrate crate, Component component) {
		super(component);
		this.painter = new GooseboyPainter(crate);
		this.fbWidth = crate.fbWidth;
		this.fbHeight = crate.fbHeight;
		this.allowsMovement = Gooseboy.getCrateMeta(crate.instance).allowsMovement;
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

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderCrate(guiGraphics, this.getLayout());
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (shouldRenderBackground())
			super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

		L layout = this.getLayout();
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				layout.bgX, layout.bgY,
				0, 0,
				layout.bgWidth, layout.bgHeight,
				layout.bgWidth, layout.bgHeight
		);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	public abstract L getLayout();

	@FunctionalInterface
	public interface LayoutSupplier<L extends CrateLayout> {
		L apply(int guiWidth, int guiHeight, int fbWidth, int fbHeight);
	}

	public void renderCrate(GuiGraphics guiGraphics, CrateLayout layout) {
		this.painter.render(guiGraphics, layout.fbX, layout.fbY, layout.fbDestWidth, layout.fbDestHeight);
	}

	public boolean shouldRenderBackground() {
		return !allowsMovement;
	}
}
