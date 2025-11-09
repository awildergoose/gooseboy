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

	private static int mapMouseToFramebuffer(boolean xAxis) {
		Minecraft mc = Minecraft.getInstance();
		var window = mc.getWindow();

		int guiW = window.getGuiScaledWidth();
		int guiH = window.getGuiScaledHeight();
		double mouseGui = xAxis
				? mc.mouseHandler.xpos() * (double) guiW / (double) window.getScreenWidth()
				: mc.mouseHandler.ypos() * (double) guiH / (double) window.getScreenHeight();

		double availableW = Math.max(1, guiW - 20);
		double availableH = Math.max(1, guiH - 20);
		double scale = Math.min(availableW / (double) CenteredCrateScreen.IMAGE_WIDTH,
								availableH / (double) CenteredCrateScreen.IMAGE_HEIGHT);

		int bgWidth = (int) Math.round(CenteredCrateScreen.IMAGE_WIDTH * scale);
		int bgHeight = (int) Math.round(CenteredCrateScreen.IMAGE_HEIGHT * scale);
		int bgX = (guiW - bgWidth) / 2;
		int bgY = (guiH - bgHeight) / 2;
		int inset = (int) Math.round(5 * scale);

		int fbGuiX = bgX + inset;
		int fbGuiY = bgY + inset;

		double fbPixelD = Math.floor((mouseGui - (xAxis ? fbGuiX : fbGuiY)) / scale);
		int fbPixel = (int) fbPixelD;

		if (fbPixel < 0) fbPixel = 0;
		if (xAxis) {
			if (fbPixel >= Gooseboy.FRAMEBUFFER_WIDTH) fbPixel = Gooseboy.FRAMEBUFFER_WIDTH - 1;
		} else {
			if (fbPixel >= Gooseboy.FRAMEBUFFER_HEIGHT) fbPixel = Gooseboy.FRAMEBUFFER_HEIGHT - 1;
		}

		return fbPixel;
	}

	public static int getMouseXInFramebuffer() {
		return mapMouseToFramebuffer(true);
	}

	public static int getMouseYInFramebuffer() {
		return mapMouseToFramebuffer(false);
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
