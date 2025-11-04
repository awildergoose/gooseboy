package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.WasmCrate;
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

public class WasmScreen extends Screen {
	private static final ResourceLocation SCREEN_UI_LOCATION = ResourceLocation.fromNamespaceAndPath(
			Gooseboy.MOD_ID, "textures/gui/wasm.png");
	private static final ResourceLocation FRAMEBUFFER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
			Gooseboy.MOD_ID, "wasm_framebuffer"
	);

	public static final int IMAGE_WIDTH = 330;
	public static final int IMAGE_HEIGHT = 256;

	private final WasmCrate crate;
	private DynamicTexture texture;
	private ByteBuffer tmpBuf;

	private boolean failed = false;

	private long lastRenderNano = 0L;
	private static final long FRAME_INTERVAL_NS = 1_000_000_000L / 60L; // 60 FPS cap

	public WasmScreen(WasmCrate crate) {
		super(Component.literal(crate.name));
		this.crate = crate;
	}

	@Override
	protected void init() {
		this.texture = new DynamicTexture("Gooseboy WASM framebuffer", FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, false);
		Minecraft.getInstance().getTextureManager().register(FRAMEBUFFER_TEXTURE, this.texture);
		this.tmpBuf = MemoryUtil.memAlloc(this.crate.fbSize);
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
		boolean shouldUpdate = (now - lastRenderNano) >= FRAME_INTERVAL_NS;

		if (shouldUpdate) {
			if (this.crate.isOk) {
				this.crate.update();
			} else if (!failed) {
				assert minecraft != null;
				SystemToast.add(minecraft.getToastManager(), SystemToast.SystemToastId.CHUNK_LOAD_FAILURE,
								Component.literal("Crate aborted during update"), Component.literal("Check the " +
																											"console for more information."));
				failed = true;
			}

			byte[] fbBytes = this.crate.getFramebufferBytes();
			tmpBuf.clear();
			tmpBuf.put(fbBytes).flip();

			var pixels = this.texture.getPixels();
			if (pixels != null)
				MemoryUtil.memCopy(MemoryUtil.memAddress(tmpBuf), pixels.getPointer(), this.crate.fbSize);

			texture.upload();
			lastRenderNano = now;
		}

		RenderSystem.setShaderTexture(0, texture.getTextureView());
		int x = ((this.width - IMAGE_WIDTH) / 2) + 5;
		int y = ((this.height - IMAGE_HEIGHT) / 2) + 5;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FRAMEBUFFER_TEXTURE, x, y, 0, 0,
						 FRAMEBUFFER_WIDTH,
						 FRAMEBUFFER_HEIGHT,
						 FRAMEBUFFER_WIDTH,
						 FRAMEBUFFER_HEIGHT);
	}

	@Override
	public void onClose() {
		if (this.tmpBuf != null) {
			MemoryUtil.memFree(this.tmpBuf);
			this.tmpBuf = null;
		}
		this.texture.close();
		this.crate.close();
		super.onClose();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderBackground(guiGraphics, i, j, f);
		int k = (this.width - IMAGE_WIDTH) / 2;
		int l = (this.height - IMAGE_HEIGHT) / 2;
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED, SCREEN_UI_LOCATION,
				k, l,
				0, 0, IMAGE_WIDTH, IMAGE_HEIGHT,
						 IMAGE_WIDTH, IMAGE_HEIGHT);
	}
}
