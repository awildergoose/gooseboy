package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.gpu.render.GooseboyGpuRenderer;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.wasm.InvalidException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static awildgoose.gooseboy.Gooseboy.withLocation;

public class GooseboyPainter implements AutoCloseable {
	private final GooseboyCrate crate;
	private final ResourceLocation framebufferTexture;
	private final long frameIntervalNano;
	private DynamicTexture texture;
	private ByteBuffer tmpBuf;
	private boolean failed = false;
	private long lastRenderNano = 0L;
	private final GooseboyGpuRenderer gpuRenderer;

	public GooseboyPainter(GooseboyCrate crate) {
		this.crate = crate;
		// Should we *really* use the frame limit (VSync, I think) option here?
		this.frameIntervalNano = 1_000_000_000L / Minecraft.getInstance().options.framerateLimit()
				.get();
		this.framebufferTexture = withLocation(
				"crate_framebuffer_" + sanitizePath(crate.name)
		);
		this.gpuRenderer = new GooseboyGpuRenderer(crate.fbWidth, crate.fbHeight);
		GooseboyClient.rendererByInstance.put(crate.instance, this.gpuRenderer);

		try {
			ExportFunction gpuMain = crate.instance.export("gpu_main");
			if (gpuMain != null)
				gpuMain.apply();
		} catch (Throwable ie) {
			if (ie instanceof InvalidException) {
				// doesn't exist, that's okay!
				return;
			}

			crate.close();
			crate.isOk = false;
			ie.printStackTrace();
		}
	}

	public void renderGpu() {
		this.gpuRenderer.render();
	}

	private static String sanitizePath(String s) {
		s = s.toLowerCase();

		return s.replaceAll("[^a-z0-9/._-]", "_");
	}

	public void initDrawing(int fbWidth, int fbHeight) {
		this.texture = new DynamicTexture(
				"Gooseboy crate framebuffer for '" + crate.name + "'", fbWidth, fbHeight, false);
		Minecraft.getInstance()
				.getTextureManager()
				.register(this.framebufferTexture, this.texture);
		this.tmpBuf = MemoryUtil.memAlloc(this.crate.fbSize);
	}

	public void close() {
		this.gpuRenderer.close();
		//noinspection resource
		GooseboyClient.rendererByInstance.remove(crate.instance);
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

		this.gpuRenderer.blitToScreen(guiGraphics, x, y, w, h);
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
