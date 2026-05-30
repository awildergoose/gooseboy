package awildgoose.gooseboy.gpu;

public final class GpuConstants {
	public static final int GB_GPU_STATUS = 0;
	public static final int GB_GPU_RECORD_ID = 4;
	public static final int GB_GPU_TEXTURE_ID = 8;
	public static final int GB_GPU_MATRIX_DEPTH = 12;

	public static int GB_STATUS_OK = 0;
	public static final int GB_STATUS_BAD_TEXTURE_SIZE = 1;
	public static final int GB_STATUS_BAD_TEXTURE = 2;
	public static final int GB_STATUS_MATRIX_TOO_SMALL = 3;
	public static final int GB_STATUS_MATRIX_TOO_BIG = 4;
	public static final int GB_STATUS_NOT_RECORDING = 5;

	private GpuConstants() {
	}
}
