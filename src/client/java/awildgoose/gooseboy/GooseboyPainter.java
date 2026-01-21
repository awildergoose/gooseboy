package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.gpu.Gooseboy3DRenderer;
import awildgoose.gooseboy.gpu.VertexStack;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static awildgoose.gooseboy.Gooseboy.*;

public class GooseboyPainter implements AutoCloseable {
	private final GooseboyCrate crate;
	private final ResourceLocation framebufferTexture;
	private final long frameIntervalNano;
	private DynamicTexture texture;
	private ByteBuffer tmpBuf;
	private boolean failed = false;
	private long lastRenderNano = 0L;
	private final Gooseboy3DRenderer renderer3D;

	public GooseboyPainter(GooseboyCrate crate) {
		this.crate = crate;
		// Should we *really* use the frame limit (VSync, I think) option here?
		this.frameIntervalNano = 1_000_000_000L / Minecraft.getInstance().options.framerateLimit()
				.get();
		this.framebufferTexture = withLocation(
				"crate_framebuffer_" + sanitizePath(crate.name)
		);
		this.renderer3D = new Gooseboy3DRenderer();
	}

	public void render3D(int x, int y) {
		this.renderer3D.render(x, y);
	}

	public static void pushCube(
			VertexStack stack,
			float x0, float y0, float z0,
			float x1, float y1, float z1
	) {
		// FRONT (+Z)
		stack.push(v(x0, y0, z1, 0, 0));
		stack.push(v(x0, y1, z1, 0, 1));
		stack.push(v(x1, y1, z1, 1, 1));
		stack.push(v(x1, y0, z1, 1, 0));

		// BACK (-Z)
		stack.push(v(x1, y0, z0, 0, 0));
		stack.push(v(x1, y1, z0, 0, 1));
		stack.push(v(x0, y1, z0, 1, 1));
		stack.push(v(x0, y0, z0, 1, 0));

		// LEFT (-X)
		stack.push(v(x0, y0, z0, 0, 0));
		stack.push(v(x0, y1, z0, 0, 1));
		stack.push(v(x0, y1, z1, 1, 1));
		stack.push(v(x0, y0, z1, 1, 0));

		// RIGHT (+X)
		stack.push(v(x1, y0, z1, 0, 0));
		stack.push(v(x1, y1, z1, 0, 1));
		stack.push(v(x1, y1, z0, 1, 1));
		stack.push(v(x1, y0, z0, 1, 0));

		// TOP (+Y)
		stack.push(v(x0, y1, z1, 0, 0));
		stack.push(v(x0, y1, z0, 0, 1));
		stack.push(v(x1, y1, z0, 1, 1));
		stack.push(v(x1, y1, z1, 1, 0));

		// BOTTOM (-Y)
		stack.push(v(x0, y0, z0, 0, 0));
		stack.push(v(x0, y0, z1, 0, 1));
		stack.push(v(x1, y0, z1, 1, 1));
		stack.push(v(x1, y0, z0, 1, 0));
	}

	private static VertexStack.Vertex v(
			float x, float y, float z,
			float u, float v
	) {
		return new VertexStack.Vertex(
				new Vector3f(x, y, z),
				new Vector2f(u, v)
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

//		guiGraphics.scissorStack.push(ScreenRectangle.of(
//				ScreenAxis.HORIZONTAL,
//				x, y,
//				w, h
//		));

//		RenderSystem.setShaderTexture(0, texture.getTextureView());
//		guiGraphics.blit(
//				RenderPipelines.GUI_TEXTURED,
//				this.framebufferTexture,
//				x, y,
//				0, 0,
//				w,
//				h,
//				w,
//				h
//		);

//		guiGraphics.scissorStack.pop();
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
