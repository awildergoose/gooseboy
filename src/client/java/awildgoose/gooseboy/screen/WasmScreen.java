package awildgoose.gooseboy.screen;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.storage.StorageCrate;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
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

public class WasmScreen extends Screen {
	private static final ResourceLocation SCREEN_UI_LOCATION = ResourceLocation.fromNamespaceAndPath(
			Gooseboy.MOD_ID, "textures/gui/wasm.png");
	private static final ResourceLocation FRAMEBUFFER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
			Gooseboy.MOD_ID, "wasm_framebuffer"
	);

	private static final int IMAGE_WIDTH = 330;
	private static final int IMAGE_HEIGHT = 256;

	public static WasmScreen INSTANCE;

	private final Instance instance;
	private DynamicTexture texture;
	private int fbPtr;
	private int fbSize;
	private ByteBuffer tmpBuf;
	private ExportFunction updateFunction;
	public StorageCrate storageCrate;
	public String instanceName;

	private long lastRenderNano = 0L;
	private static final long FRAME_INTERVAL_NS = 1_000_000_000L / 60L; // 60 FPS cap

	public WasmScreen(Instance instance, String instanceName) {
		super(Component.literal(instanceName));
		this.instance = instance;
		this.instanceName = instanceName;
		INSTANCE = this;
	}

	public void clear(int color) {
		var mem = this.instance.memory();
		int p = this.fbPtr;

		for (int i = 0; i < fbSize; i += 4) {
			mem.writeI32(p + i, color);
		}
	}

	public int getMouseXInFramebuffer() {
		double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double)this.width / (double)Minecraft.getInstance().getWindow().getScreenWidth();
		int fbX = (int)(mouseX - (((this.width - IMAGE_WIDTH) / 2) + 5));
		if (fbX < 0) fbX = 0;
		if (fbX >= FRAMEBUFFER_WIDTH) fbX = FRAMEBUFFER_WIDTH - 1;
		return fbX;
	}

	public int getMouseYInFramebuffer() {
		double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double)this.height / (double)Minecraft.getInstance().getWindow().getScreenHeight();
		int fbY = (int)(mouseY - (((this.height - IMAGE_HEIGHT) / 2) + 5));
		if (fbY < 0) fbY = 0;
		if (fbY >= FRAMEBUFFER_HEIGHT) fbY = FRAMEBUFFER_HEIGHT - 1;
		return fbY;
	}

	@Override
	protected void init() {
		// TODO make sure the functions we're calling or exporting do exist!
		this.texture = new DynamicTexture("Gooseboy WASM framebuffer", FRAMEBUFFER_WIDTH, FRAMEBUFFER_HEIGHT, false);
		Minecraft.getInstance().getTextureManager().register(FRAMEBUFFER_TEXTURE, this.texture);
		this.fbPtr = (int) this.instance.export("get_framebuffer_ptr").apply()[0];
		this.fbSize = FRAMEBUFFER_WIDTH * FRAMEBUFFER_HEIGHT * 4;
		this.tmpBuf = MemoryUtil.memAlloc(this.fbSize);
		this.updateFunction = this.instance.export("update");
		this.storageCrate = new StorageCrate(this.instanceName);
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
			this.updateFunction.apply(now);

			byte[] fbBytes = instance.memory().readBytes(this.fbPtr, this.fbSize);
			tmpBuf.clear();
			tmpBuf.put(fbBytes).flip();

			var pixels = this.texture.getPixels();
			if (pixels != null)
				MemoryUtil.memCopy(MemoryUtil.memAddress(tmpBuf), pixels.getPointer(), this.fbSize);

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
		INSTANCE = null;
		if (this.tmpBuf != null) {
			MemoryUtil.memFree(this.tmpBuf);
			this.tmpBuf = null;
		}
		this.texture.close();
		this.storageCrate.save();
		// close wasm?
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
