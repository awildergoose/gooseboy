package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class ClientCommonBridgeImpl implements ClientCommonBridge {
	@Override
	public boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
	}

	@Override
	public boolean isMouseButtonDown(int button) {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
	}

	@Override
	public int getMouseXInFramebuffer() {
		int width = Minecraft.getInstance().getWindow().getScreenWidth();
		double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double)width / (double)Minecraft.getInstance().getWindow().getScreenWidth();
		int fbX = (int)(mouseX - (((width - WasmScreen.IMAGE_WIDTH) / 2) + 5));
		if (fbX < 0) fbX = 0;
		if (fbX >= FRAMEBUFFER_WIDTH) fbX = FRAMEBUFFER_WIDTH - 1;
		return fbX;
	}

	@Override
	public int getMouseYInFramebuffer() {
		int height = Minecraft.getInstance().getWindow().getScreenHeight();
		double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double)height / (double)Minecraft.getInstance().getWindow().getScreenHeight();
		int fbY = (int)(mouseY - (((height - WasmScreen.IMAGE_HEIGHT) / 2) + 5));
		if (fbY < 0) fbY = 0;
		if (fbY >= FRAMEBUFFER_HEIGHT) fbY = FRAMEBUFFER_HEIGHT - 1;
		return fbY;
	}

	@Override
	public void playRawAudio(byte[] pcm) {
		RawAudioManager.play(pcm);
	}
}
