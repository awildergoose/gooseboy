package awildgoose.gooseboy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RawImage {
	public final int width;
	public final int height;
	public final int[] pixels; // ARGB

	private RawImage(int w, int h, int[] p) {
		width = w;
		height = h;
		pixels = p;
	}

	public static RawImage load(ByteArrayInputStream bf) throws IOException {
		BufferedImage img = ImageIO.read(bf);
		if (img == null)
			throw new IOException("invalid or unsupported image");

		int w = img.getWidth();
		int h = img.getHeight();

		if (img.getType() == BufferedImage.TYPE_INT_ARGB) {
			int[] raw = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			return new RawImage(w, h, raw.clone());
		}

		BufferedImage argb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		argb.getGraphics().drawImage(img, 0, 0, null);

		int[] raw = ((DataBufferInt) argb.getRaster().getDataBuffer()).getData();
		return new RawImage(w, h, raw.clone());
	}
}
