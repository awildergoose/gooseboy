package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.WasmCrate;
import net.minecraft.client.Minecraft;

public class ClientCommonBridgeImpl implements ClientCommonBridge {
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
	public int getWindowWidth() {
		return Minecraft.getInstance().getWindow().getWidth();
	}

	@Override
	public int getWindowHeight() {
		return Minecraft.getInstance().getWindow().getHeight();
	}

	@Override
	public void closeCrate(WasmCrate crate) {
		RawAudioManager.stopAllSounds();
		WasmInputManager.releaseMouse();
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
