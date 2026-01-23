package awildgoose.gooseboy.screen.renderer;

import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.screen.layout.CrateLayout;
import net.minecraft.network.chat.Component;

public class BottomRightCrateScreen extends CrateRendererScreen<BottomRightCrateScreen.Layout> {
	public BottomRightCrateScreen(GooseboyCrate crate) {
		super(crate, Component.literal(crate.name));
	}

	@Override
	public Layout getLayout() {
		return Layout.forSize(this.width, this.height, this.fbWidth, this.fbHeight);
	}

	public static final class Layout extends CrateLayout {
		private static final int GUI_PADDING_X = 20;
		private static final int GUI_PADDING_Y = 20;
		private static final int INSET_PIXELS = 5;
		private static final double MAX_SCALE = 1.0;

		public Layout(double scale, int bgWidth, int bgHeight, int bgX, int bgY, int fbDestWidth, int fbDestHeight,
					  int inset, int fbX, int fbY, int fbW, int fbH) {
			super(scale, bgWidth, bgHeight, bgX, bgY, fbDestWidth, fbDestHeight, inset, fbX, fbY, fbW, fbH);
		}

		public static Layout forSize(int guiWidth, int guiHeight, int fbWidth, int fbHeight) {
			// note: this function is entirely AI, UI layout is not my expertise.
			double availableW = Math.max(1, guiWidth - GUI_PADDING_X * 2);
			double availableH = Math.max(1, guiHeight - GUI_PADDING_Y * 2);

			double scale = Math.min(availableW / (double) IMAGE_WIDTH, availableH / (double) IMAGE_HEIGHT);
			scale = Math.min(scale, MAX_SCALE);

			int bgWidth = (int) Math.round(IMAGE_WIDTH * scale);
			int bgHeight = (int) Math.round(IMAGE_HEIGHT * scale);

			int bgX = guiWidth - bgWidth - GUI_PADDING_X;
			if (bgX < 0) bgX = 0;

			int bgY = guiHeight - bgHeight - GUI_PADDING_Y;
			if (bgY < 0) bgY = 0;

			int fbDestWidth = (int) Math.round(fbWidth * scale);
			int fbDestHeight = (int) Math.round(fbHeight * scale);
			int inset = (int) Math.round(INSET_PIXELS * scale);

			int fbX = bgX + inset;
			int fbY = bgY + inset;

			return new Layout(scale, bgWidth, bgHeight, bgX, bgY,
							  fbDestWidth, fbDestHeight, inset, fbX, fbY, fbWidth, fbHeight);
		}
	}
}
