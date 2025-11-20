package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class ClientCommonBridgeImpl implements ClientCommonBridge {
	@Override
	public void doErrorMessage(String title, String body) {
		SystemToast.add(Minecraft.getInstance()
								.getToastManager(), SystemToast.SystemToastId.CHUNK_LOAD_FAILURE,
						Component.literal(title), Component.literal(body)
		);
		Gooseboy.LOGGER.error("%s: %s".formatted(title, body));
	}

	@Override
	public void doTranslatedErrorMessage(String title, String body, Object... o) {
		Component titleC = Component.translatable(title);
		Component bodyC = Component.translatable(body, o);

		SystemToast.add(Minecraft.getInstance()
								.getToastManager(), SystemToast.SystemToastId.CHUNK_LOAD_FAILURE, titleC, bodyC);

		Gooseboy.LOGGER.error("%s: %s".formatted(titleC.getString(), bodyC.getString()));
	}

	@Override
	public int getKeyCode() {
		return WasmInputManager.getKeyCode();
	}

	@Override
	public boolean isKeyDown(int key) {
		return WasmInputManager.isKeyDown(key);
	}

	@Override
	public boolean isMouseButtonDown(int button) {
		return WasmInputManager.isMouseButtonDown(button);
	}

	@Override
	public int getMouseXInFramebuffer() {
		return WasmInputManager.getMouseXInFramebuffer();
	}

	@Override
	public int getMouseYInFramebuffer() {
		return WasmInputManager.getMouseYInFramebuffer();
	}

	@Override
	public void grabMouse() {
		WasmInputManager.grabMouse();
	}

	@Override
	public void releaseMouse() {
		WasmInputManager.releaseMouse();
	}

	@Override
	public double getMouseAccumulatedDX() {
		return WasmInputManager.LAST_ACCUMULATED_MOUSE_X;
	}

	@Override
	public double getMouseAccumulatedDY() {
		return WasmInputManager.LAST_ACCUMULATED_MOUSE_Y;
	}

	@Override
	public boolean isMouseGrabbed() {
		return Minecraft.getInstance().mouseHandler.isMouseGrabbed();
	}

	@Override
	public void closeCrate(GooseboyCrate crate) {
		RawAudioManager.stopAllSounds();
		WasmInputManager.releaseMouse();
	}

	@Override
	public void warnPermission(GooseboyCrate.Permission permission) {
		SystemToast.add(
				Minecraft.getInstance()
						.getToastManager(),
				SystemToast.SystemToastId.FILE_DROP_FAILURE,
				Component.translatable("ui.gooseboy.missing_permissions.title"),
				Component.translatable("ui.gooseboy.missing_permissions.body", permission.name())
		);
	}

	@Override
	public long playRawAudio(byte[] pcm) {
		return RawAudioManager.play(pcm);
	}

	@Override
	public void stopAudio(long id) {
		RawAudioManager.stop(id);
	}

	@Override
	public void stopAllAudio() {
		RawAudioManager.stopAllSounds();
	}

	@Override
	public void setAudioVolume(long id, float volume) {
		RawAudioManager.setVolume(id, volume);
	}

	@Override
	public void setAudioPitch(long id, float pitch) {
		RawAudioManager.setPitch(id, pitch);
	}

	@Override
	public boolean isAudioPlaying(long id) {
		return RawAudioManager.isPlaying(id);
	}
}
