package awildgoose.gooseboy;

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
	public long playRawAudio(byte[] pcm) {
		return RawAudioManager.play(pcm);
	}

	@Override
	public void stopAudio(long id) {
		RawAudioManager.stop(id);
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
