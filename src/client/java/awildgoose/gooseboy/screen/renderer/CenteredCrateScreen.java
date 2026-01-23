package awildgoose.gooseboy.screen.renderer;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class CenteredCrateScreen extends CrateRendererScreen {
	private static final ResourceLocation SCREEN_UI_LOCATION = Gooseboy.withLocation("textures/gui/wasm.png");
	public static final int IMAGE_WIDTH = 330;
	public static final int IMAGE_HEIGHT = 214;

	public CenteredCrateScreen(GooseboyCrate crate) {
		super(crate, Component.literal(crate.name));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderCrate(guiGraphics, this.getLayout());
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

		Layout layout = Layout.forSize(this.width, this.height);
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				layout.bgX, layout.bgY,
				0, 0,
				layout.bgWidth, layout.bgHeight,
				layout.bgWidth, layout.bgHeight
		);
	}

	@Override
	public CrateLayout getLayout() {
		return Layout.forSize(this.width, this.height);
	}

	public static final class Layout extends CrateLayout {
		private static final int GUI_PADDING = 20;
		private static final int INSET_PIXELS = 5;

		public Layout(double scale, int bgWidth, int bgHeight, int bgX, int bgY, int fbDestWidth, int fbDestHeight, int inset, int fbX, int fbY) {
			super(scale, bgWidth, bgHeight, bgX, bgY, fbDestWidth, fbDestHeight, inset, fbX, fbY);
		}

		public static Layout forSize(int guiWidth, int guiHeight) {
			double availableW = Math.max(1, guiWidth - GUI_PADDING);
			double availableH = Math.max(1, guiHeight - GUI_PADDING);
			double scale = Math.min(availableW / (double) IMAGE_WIDTH, availableH / (double) IMAGE_HEIGHT);

			int bgWidth = (int) Math.round(IMAGE_WIDTH * scale);
			int bgHeight = (int) Math.round(IMAGE_HEIGHT * scale);
			int bgX = (guiWidth - bgWidth) / 2;
			int bgY = (guiHeight - bgHeight) / 2;

			int fbDestWidth = (int) Math.round(FRAMEBUFFER_WIDTH * scale);
			int fbDestHeight = (int) Math.round(FRAMEBUFFER_HEIGHT * scale);
			int inset = (int) Math.round(INSET_PIXELS * scale);
			int fbX = bgX + inset;
			int fbY = bgY + inset;

			return new Layout(scale, bgWidth, bgHeight, bgX, bgY, fbDestWidth, fbDestHeight, inset, fbX, fbY);
		}
	}
}
