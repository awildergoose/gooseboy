package awildgoose.gooseboy;

import awildgoose.gooseboy.mixin.client.MouseHandlerAccessor;
import awildgoose.gooseboy.screen.CenteredCrateScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class WasmInputManager {
	private static final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1];
	private static final Queue<Integer> keyQueue = new ArrayDeque<>();

	// These values are set from a mixin right before the values get reset to 0s
	public static double LAST_ACCUMULATED_MOUSE_X = 0;
	public static double LAST_ACCUMULATED_MOUSE_Y = 0;

	public static void update() {
		long window = Minecraft.getInstance().getWindow().handle();

		for (int i = 32; i <= GLFW.GLFW_KEY_LAST; i++) {
			boolean down = GLFW.glfwGetKey(window, i) == GLFW.GLFW_PRESS;
			if (down && !keys[i]) {
				keyQueue.add(i);
			}
			keys[i] = down;
		}
	}

	public static int getKeyCode() {
		Integer key = keyQueue.poll();
		return key != null ? key : -1;
	}

	public static boolean isKeyDown(int key) {
		return key >= 32 && key <= GLFW.GLFW_KEY_LAST && keys[key];
	}

	public static void reset() {
		keyQueue.clear();
		Arrays.fill(keys, false);
	}

	public static boolean isMouseButtonDown(int button) {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
	}

	public static int getMouseXInFramebuffer() {
		int width = Minecraft.getInstance().getWindow().getScreenWidth() / 2;
		double mouseX =
				Minecraft.getInstance().mouseHandler.xpos() * (double) width / (double)Minecraft.getInstance().getWindow().getScreenWidth();
		int fbX = (int)(mouseX - (((width - CenteredCrateScreen.IMAGE_WIDTH) / 2) + 5));
		if (fbX < 0) fbX = 0;
		if (fbX >= Gooseboy.FRAMEBUFFER_WIDTH) fbX = Gooseboy.FRAMEBUFFER_WIDTH - 1;
		return fbX;
	}

	public static int getMouseYInFramebuffer() {
		int height = Minecraft.getInstance().getWindow().getScreenHeight() / 2;
		double mouseY =
				Minecraft.getInstance().mouseHandler.ypos() * (double) height / (double) Minecraft.getInstance().getWindow().getScreenHeight();
		int fbY = (int)(mouseY - (((height - CenteredCrateScreen.IMAGE_HEIGHT) / 2) + 5));
		if (fbY < 0) fbY = 0;
		if (fbY >= Gooseboy.FRAMEBUFFER_HEIGHT) fbY = Gooseboy.FRAMEBUFFER_HEIGHT - 1;
		return fbY;
	}

	public static void grabMouse() {
		var minecraft = Minecraft.getInstance();
		var mouseHandler = minecraft.mouseHandler;
		if (minecraft.isWindowActive()) {
			//noinspection ReferenceToMixin
			var accessor = (MouseHandlerAccessor)mouseHandler;
			if (!accessor.gooseboy$isMouseGrabbed()) {
				if (InputQuirks.RESTORE_KEY_STATE_AFTER_MOUSE_GRAB) {
					KeyMapping.setAll();
				}

				accessor.gooseboy$setMouseGrabbed(true);
				var xPos = minecraft
						.getWindow()
						.getScreenWidth() / 2;
				var yPos = minecraft
						.getWindow()
						.getScreenHeight() / 2;
				accessor.gooseboy$setXPos(xPos);
				accessor.gooseboy$setYPos(yPos);
				InputConstants.grabOrReleaseMouse(
						minecraft.getWindow(), 212995,
						xPos, yPos);
	//			minecraft.setScreen(null);
				minecraft.missTime = 10000;
				mouseHandler.cursorEntered();
			}
		}
	}

	public static void releaseMouse() {
		Minecraft.getInstance().mouseHandler.releaseMouse();
	}
}
