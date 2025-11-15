package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class CenteredCrateScreen extends Screen {
	private static final ResourceLocation SCREEN_UI_LOCATION = ResourceLocation.fromNamespaceAndPath(
			Gooseboy.MOD_ID, "textures/gui/wasm.png");
	public static final int IMAGE_WIDTH = 330;
	public static final int IMAGE_HEIGHT = 214;

	private static final int GUI_PADDING = 20;
	private static final int INSET_PIXELS = 5;

	private final GooseboyCrate crate;
	private final ResourceLocation framebufferTexture;
	private DynamicTexture texture;
	private ByteBuffer tmpBuf;

	private boolean failed = false;

	private long lastRenderNano = 0L;
	private final long frameIntervalNano;

	public CenteredCrateScreen(GooseboyCrate crate) {
		super(Component.literal(crate.name));
		this.crate = crate;
		// Should we *really* use the frame limit option here?
		this.frameIntervalNano = 1_000_000_000L / Minecraft.getInstance().options.framerateLimit().get();
		this.framebufferTexture =  ResourceLocation.fromNamespaceAndPath(
				Gooseboy.MOD_ID, "crate_framebuffer_" + crate.name
		);
	}

	@Override
	protected void init() {
		this.texture = new DynamicTexture("Gooseboy crate framebuffer", FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, false);
		Minecraft.getInstance().getTextureManager().register(this.framebufferTexture, this.texture);
		this.tmpBuf = MemoryUtil.memAlloc(this.crate.fbSize);
	}

	@Override
	public void onClose() {
		if (this.tmpBuf != null) {
			MemoryUtil.memFree(this.tmpBuf);
			this.tmpBuf = null;
		}
		this.texture.close();
		Minecraft.getInstance().getTextureManager().release(this.framebufferTexture);
		this.crate.close();
		super.onClose();
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

		long now = System.nanoTime();
		updateTextureIfNeeded(now);

		Layout layout = Layout.forSize(this.width, this.height);

		RenderSystem.setShaderTexture(0, texture.getTextureView());
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				this.framebufferTexture,
				layout.fbX, layout.fbY,
				0, 0,
				layout.fbDestWidth,
				layout.fbDestHeight,
				layout.fbDestWidth,
				layout.fbDestHeight
		);
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

	private void updateTextureIfNeeded(long now) {
		boolean shouldUpdate = (now - lastRenderNano) >= this.frameIntervalNano;
		if (!shouldUpdate) return;

		if (this.crate.isOk) {
			this.crate.update();

			byte[] fbBytes = this.crate.getFramebufferBytes();

			if (tmpBuf != null) {
				tmpBuf.clear();
				tmpBuf.put(fbBytes).flip();

				var pixels = this.texture.getPixels();
				if (pixels != null) {
					MemoryUtil.memCopy(MemoryUtil.memAddress(tmpBuf), pixels.getPointer(), this.crate.fbSize);
				}
				texture.upload();
			}

			lastRenderNano = now;
		} else if (!failed) {
			assert minecraft != null;

			Gooseboy.ccb.doErrorMessage("Crate aborted during update",
							"Check the console for more information.");
			failed = true;
		}
	}

	private record Layout(double scale, int bgWidth, int bgHeight, int bgX, int bgY, int fbDestWidth, int fbDestHeight,
						  int inset, int fbX, int fbY) {
		static Layout forSize(int guiWidth, int guiHeight) {
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
