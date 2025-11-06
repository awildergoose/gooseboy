package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.WasmCrate;

public interface ClientCommonBridge {
	// input
	int getKeyCode();
	boolean isKeyDown(int key);
	boolean isMouseButtonDown(int button);
	int getMouseXInFramebuffer();
	int getMouseYInFramebuffer();
	void grabMouse();
	void releaseMouse();
	double getMouseAccumulatedDX();
	double getMouseAccumulatedDY();
	boolean isMouseGrabbed();

	// audio
	long playRawAudio(byte[] pcm);
	void stopAudio(long id);
	void stopAllAudio();
	void setAudioVolume(long id, float volume);
	void setAudioPitch(long id, float pitch);
	boolean isAudioPlaying(long id);

	// game
	int getWindowWidth();
	int getWindowHeight();

	void closeCrate(WasmCrate crate);
}
