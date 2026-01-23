package awildgoose.gooseboy.screen.layout;

public abstract class CrateLayout {
	public double scale;
	public int bgWidth;
	public int bgHeight;
	public int bgX;
	public int bgY;
	public int fbDestWidth;
	public int fbDestHeight;
	public int inset;
	public int fbX;
	public int fbY;

	public CrateLayout(double scale, int bgWidth, int bgHeight, int bgX, int bgY, int fbDestWidth, int fbDestHeight, int inset, int fbX, int fbY) {
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
	}
}
