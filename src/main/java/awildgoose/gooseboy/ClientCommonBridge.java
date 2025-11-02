package awildgoose.gooseboy;

import awildgoose.gooseboy.storage.StorageCrate;

public interface ClientCommonBridge {
	void clear(int color);
	boolean isKeyDown(int key);
	boolean isMouseButtonDown(int button);
	int getMouseXInFramebuffer();
	int getMouseYInFramebuffer();
	void playRawAudio(byte[] pcm);
	StorageCrate getStorageCrate();
}
