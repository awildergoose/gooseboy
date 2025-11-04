package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;


public class WasmInputManager {
	private static final int MAX_KEYS = GLFW.GLFW_KEY_LAST;
	private static final boolean[] keys = new boolean[MAX_KEYS];

	public static void update() {
		long window = Minecraft.getInstance().getWindow().handle();
		for (int i = 0; i < MAX_KEYS; i++) {
			keys[i] = GLFW.glfwGetKey(window, i) == GLFW.GLFW_PRESS;
		}
	}

	public static int getKeyCode() {
		for (int i = 0; i < MAX_KEYS; i++) {
			if (keys[i]) return i;
		}
		return -1;
	}

	public static boolean isKeyDown(int key) {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
	}

	public static boolean isMouseButtonDown(int button) {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
	}

	public static int getMouseXInFramebuffer() {
		int width = Minecraft.getInstance().getWindow().getScreenWidth() / 2;
		double mouseX =
				Minecraft.getInstance().mouseHandler.xpos() * (double) width / (double)Minecraft.getInstance().getWindow().getScreenWidth();
		int fbX = (int)(mouseX - (((width - WasmScreen.IMAGE_WIDTH) / 2) + 5));
		if (fbX < 0) fbX = 0;
		if (fbX >= Gooseboy.FRAMEBUFFER_WIDTH) fbX = Gooseboy.FRAMEBUFFER_WIDTH - 1;
		return fbX;
	}

	public static int getMouseYInFramebuffer() {
		int height = Minecraft.getInstance().getWindow().getScreenHeight() / 2;
		double mouseY =
				Minecraft.getInstance().mouseHandler.ypos() * (double) height / (double) Minecraft.getInstance().getWindow().getScreenHeight();
		int fbY = (int)(mouseY - (((height - WasmScreen.IMAGE_HEIGHT) / 2) + 5));
		if (fbY < 0) fbY = 0;
		if (fbY >= Gooseboy.FRAMEBUFFER_HEIGHT) fbY = Gooseboy.FRAMEBUFFER_HEIGHT - 1;
		return fbY;
	}
}
