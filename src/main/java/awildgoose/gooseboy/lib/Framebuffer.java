package awildgoose.gooseboy.lib;


import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;

@HostModule("framebuffer")
public final class Framebuffer {
	public Framebuffer() {
	}

	@WasmExport
	public int get_framebuffer_width(Instance instance) {
		return Gooseboy.getCrate(instance).fbWidth;
	}

	@WasmExport
	public int get_framebuffer_height(Instance instance) {
		return Gooseboy.getCrate(instance).fbHeight;
	}

	@WasmExport
	public void clear_surface(Instance instance, int ptr, int size, int color) {
		Gooseboy.getCrate(instance)
				.clearSurface(ptr, size, color);
	}

	@WasmExport
	public void blit_premultiplied_clipped(Instance instance, int destPtr, int dstW, int dstH, int destX, int destY,
										   int srcW, int srcH,
										   int srcPtr,
										   int blendI) {
		boolean blend = blendI != 0;

		if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) return;
		long srcPixels = (long) srcW * (long) srcH;
		long srcBytesLong = srcPixels * 4L;
		if (srcBytesLong <= 0 || srcBytesLong > Integer.MAX_VALUE) return;
		int srcBytes = (int) srcBytesLong;

		Memory mem = instance.memory();

		byte[] src = mem.readBytes(srcPtr, srcBytes);

		int srcRight = destX + srcW;
		int srcBottom = destY + srcH;

		int visLeft = Math.max(destX, 0);
		int visTop = Math.max(destY, 0);
		int visRight = Math.min(srcRight, dstW);
		int visBottom = Math.min(srcBottom, dstH);

		if (visLeft >= visRight || visTop >= visBottom) return;

		int startSrcX = visLeft - destX;
		int startSrcY = visTop - destY;

		int visW = visRight - visLeft;
		int visH = visBottom - visTop;
		int destRowBytes = dstW * 4;
		int visRowBytes = visW * 4;

		for (int row = 0; row < visH; row++) {
			int dstY = visTop + row;
			long dstOffsetLong = (long) dstY * destRowBytes + (long) visLeft * 4L;
			if (dstOffsetLong < 0 || dstOffsetLong > Integer.MAX_VALUE) return;
			int dstOffset = (int) dstOffsetLong;
			byte[] destRow = mem.readBytes(destPtr + dstOffset, visRowBytes);

			int srcRow = (startSrcY + row) * srcW * 4;

			for (int col = 0; col < visW; col++) {
				int sidx = srcRow + (startSrcX + col) * 4;
				int didx = col * 4;

				int sa = src[sidx + 3] & 0xFF;
				if (sa == 0) {
					continue;
				}

				if (!blend || sa == 255) {
					destRow[didx] = src[sidx];
					destRow[didx + 1] = src[sidx + 1];
					destRow[didx + 2] = src[sidx + 2];
					destRow[didx + 3] = src[sidx + 3];
					continue;
				}

				int inv = 255 - sa;

				int sr = src[sidx] & 0xFF;
				int sg = src[sidx + 1] & 0xFF;
				int sb = src[sidx + 2] & 0xFF;

				int dr = destRow[didx] & 0xFF;
				int dg = destRow[didx + 1] & 0xFF;
				int db = destRow[didx + 2] & 0xFF;
				int da = destRow[didx + 3] & 0xFF;

				int out_r = sr + ((dr * inv + 127) / 255);
				int out_g = sg + ((dg * inv + 127) / 255);
				int out_b = sb + ((db * inv + 127) / 255);
				int out_a = sa + ((da * inv + 127) / 255);

				destRow[didx] = (byte) (Math.min(out_r, 255));
				destRow[didx + 1] = (byte) (Math.min(out_g, 255));
				destRow[didx + 2] = (byte) (Math.min(out_b, 255));
				destRow[didx + 3] = (byte) (Math.min(out_a, 255));
			}

			mem.write(destPtr + dstOffset, destRow);
		}
	}

	public HostFunction[] toHostFunctions() {
		return Framebuffer_ModuleFactory.toHostFunctions(this);
	}
}
