package awildgoose.gooseboy;

public interface ClientCommonBridge {
	boolean isKeyDown(int key);
	boolean isMouseButtonDown(int button);
	int getMouseXInFramebuffer();
	int getMouseYInFramebuffer();
	void playRawAudio(byte[] pcm);
}
