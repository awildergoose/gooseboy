package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;

public interface ClientCommonBridge {
	// misc
	void doErrorMessage(String title, String body);

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

	void closeCrate(GooseboyCrate crate);
	void warnPermission(GooseboyCrate.Permission permission);
}
