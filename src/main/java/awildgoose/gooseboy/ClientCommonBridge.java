package awildgoose.gooseboy;

public interface ClientCommonBridge {
	void clear(int color);
	boolean isKeyDown(int key);
	boolean isMouseButtonDown(int button);
	int getMouseXInFramebuffer();
	int getMouseYInFramebuffer();
}
