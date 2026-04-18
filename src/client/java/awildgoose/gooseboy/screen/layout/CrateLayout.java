package awildgoose.gooseboy.screen.layout;

public abstract class CrateLayout {
	public final double scale;
	public final int bgWidth;
	public final int bgHeight;
	public final int bgX;
	public final int bgY;
	public final int fbDestWidth;
	public final int fbDestHeight;
	public final int inset;
	public final int fbX;
	public final int fbY;
	public final int fbW;
	public final int fbH;

	public CrateLayout(double scale, int bgWidth, int bgHeight, int bgX, int bgY, int fbDestWidth, int fbDestHeight,
	                   int inset, int fbX, int fbY, int fbW, int fbH) {
		this.scale = scale;
		this.bgWidth = bgWidth;
		this.bgHeight = bgHeight;
		this.bgX = bgX;
		this.bgY = bgY;
		this.fbDestWidth = fbDestWidth;
		this.fbDestHeight = fbDestHeight;
		this.inset = inset;
		this.fbX = fbX;
		this.fbY = fbY;
		this.fbW = fbW;
		this.fbH = fbH;
	}
}
