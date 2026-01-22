package awildgoose.gooseboy.gpu;

public class GooseboyGpuMemoryReader implements GooseboyGpu.MemoryReadOffsetConsumer {
	private final byte[] bytes;

	public GooseboyGpuMemoryReader(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public int readInt(int offset) {
		return (bytes[offset] & 0xFF) |
				((bytes[offset + 1] & 0xFF) << 8) |
				((bytes[offset + 2] & 0xFF) << 16) |
				((bytes[offset + 3] & 0xFF) << 24);
	}

	@Override
	public float readFloat(int offset) {
		return Float.intBitsToFloat(readInt(offset));
	}

	@Override
	public byte[] readBytes(int offset, int len) {
		byte[] result = new byte[len];
		System.arraycopy(bytes, offset, result, 0, len);
		return result;
	}
}
