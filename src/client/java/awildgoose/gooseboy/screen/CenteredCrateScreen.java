package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);

		long now = System.nanoTime();
		boolean shouldUpdate = (now - lastRenderNano) >= this.frameIntervalNano;

		if (shouldUpdate) {
			if (this.crate.isOk) {
				this.crate.update();

				byte[] fbBytes = this.crate.getFramebufferBytes();
				tmpBuf.clear();
				tmpBuf.put(fbBytes).flip();

				var pixels = this.texture.getPixels();
				if (pixels != null)
					MemoryUtil.memCopy(MemoryUtil.memAddress(tmpBuf), pixels.getPointer(), this.crate.fbSize);

				texture.upload();
				lastRenderNano = now;
			} else if (!failed) {
				assert minecraft != null;
				SystemToast.add(minecraft.getToastManager(), SystemToast.SystemToastId.CHUNK_LOAD_FAILURE,
								Component.literal("Crate aborted during update"), Component.literal("Check the " +
																											"console for more information."));
				failed = true;
			}
		}

		double availableW = Math.max(1, this.width - 20);
		double availableH = Math.max(1, this.height - 20);
		double scale = Math.min(availableW / (double) IMAGE_WIDTH, availableH / (double) IMAGE_HEIGHT);

		int bgWidth = (int) Math.round(IMAGE_WIDTH * scale);
		int bgHeight = (int) Math.round(IMAGE_HEIGHT * scale);

		int fbDestWidth = (int) Math.round(FRAMEBUFFER_WIDTH * scale);
		int fbDestHeight = (int) Math.round(FRAMEBUFFER_HEIGHT * scale);

		int bgX = (this.width - bgWidth) / 2;
		int bgY = (this.height - bgHeight) / 2;

		int inset = (int) Math.round(5 * scale);
		int fbX = bgX + inset;
		int fbY = bgY + inset;

		RenderSystem.setShaderTexture(0, texture.getTextureView());
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				this.framebufferTexture,
				fbX, fbY,
				0, 0,
				fbDestWidth,
				fbDestHeight,
				fbDestWidth,
				fbDestHeight
		);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderBackground(guiGraphics, i, j, f);

		double availableW = Math.max(1, this.width - 20);
		double availableH = Math.max(1, this.height - 20);
		double scale = Math.min(availableW / (double) IMAGE_WIDTH, availableH / (double) IMAGE_HEIGHT);

		int bgWidth = (int) Math.round(IMAGE_WIDTH * scale);
		int bgHeight = (int) Math.round(IMAGE_HEIGHT * scale);
		int bgX = (this.width - bgWidth) / 2;
		int bgY = (this.height - bgHeight) / 2;

		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				bgX, bgY,
				0, 0,
				bgWidth, bgHeight,
				bgWidth, bgHeight
		);
	}
}
