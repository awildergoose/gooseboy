package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmScreen;
import awildgoose.gooseboy.storage.StorageCrate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ClientCommonBridgeImpl implements ClientCommonBridge {
	@Override
	public void clear(int color) {
		WasmScreen.INSTANCE.clear(color);
	}

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
		return WasmScreen.INSTANCE.getMouseXInFramebuffer();
	}

	@Override
	public int getMouseYInFramebuffer() {
		return WasmScreen.INSTANCE.getMouseYInFramebuffer();
	}

	@Override
	public void playRawAudio(byte[] pcm) {
		RawAudioManager.play(pcm);
	}

	@Override
	public StorageCrate getStorageCrate() {
		return WasmScreen.INSTANCE.storageCrate;
	}
}
