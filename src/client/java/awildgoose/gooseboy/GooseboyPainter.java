package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class GooseboyPainter implements AutoCloseable {
	private final GooseboyCrate crate;
	private final ResourceLocation framebufferTexture;
	private final long frameIntervalNano;
	private DynamicTexture texture;
	private ByteBuffer tmpBuf;
	private boolean failed = false;
	private long lastRenderNano = 0L;

	public GooseboyPainter(GooseboyCrate crate) {
		this.crate = crate;
		// Should we *really* use the frame limit (VSync, I think) option here?
		this.frameIntervalNano = 1_000_000_000L / Minecraft.getInstance().options.framerateLimit()
				.get();
		this.framebufferTexture = ResourceLocation.fromNamespaceAndPath(
				Gooseboy.MOD_ID, "crate_framebuffer_" + sanitizePath(crate.name)
		);
	}

	private static String sanitizePath(String s) {
		s = s.toLowerCase();

		return s.replaceAll("[^a-z0-9/._-]", "_");
	}

	public void initDrawing() {
		this.texture = new DynamicTexture(
				"Gooseboy crate framebuffer for '" + crate.name + "'", FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, false);
		Minecraft.getInstance()
				.getTextureManager()
				.register(this.framebufferTexture, this.texture);
		this.tmpBuf = MemoryUtil.memAlloc(this.crate.fbSize);
	}

	public void close() {
		if (this.tmpBuf != null) {
			MemoryUtil.memFree(this.tmpBuf);
			this.tmpBuf = null;
		}
		this.texture.close();
		Minecraft.getInstance()
				.getTextureManager()
				.release(this.framebufferTexture);
		this.crate.close();
	}

	public void render(GuiGraphics guiGraphics, int x, int y, int w, int h) {
		long now = System.nanoTime();
		updateTextureIfNeeded(now);

		RenderSystem.setShaderTexture(0, texture.getTextureView());
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				this.framebufferTexture,
				x, y,
				0, 0,
				w,
				h,
				w,
				h
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
				tmpBuf.put(fbBytes)
						.flip();

				NativeImage pixels = this.texture.getPixels();
				if (pixels != null) {
					MemoryUtil.memCopy(MemoryUtil.memAddress(tmpBuf), pixels.getPointer(), this.crate.fbSize);
				}
				texture.upload();
			}

			lastRenderNano = now;
		} else if (!failed) {
			Gooseboy.ccb.doTranslatedErrorMessage(
					"ui.gooseboy.crate_update_failed.title",
					"ui.gooseboy.crate_update_failed.body");
			failed = true;
		}
	}
}
