package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.Gooseboy;

public class GpuMemoryReader implements MemoryReadConsumer {
	private final byte[] bytes;

	public GpuMemoryReader(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public int readInt(int offset) {
		if (offset < 0 || (long) offset + 4 > bytes.length) {
			Gooseboy.LOGGER.warn("GpuMemoryReader.readInt: offset {} beyond end (capacity={})", offset, bytes.length);
			return 0;
		}

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
		long end = (long) offset + (long) len;

		if (end > bytes.length) {
			Gooseboy.LOGGER.warn("GpuMemoryReader.readBytes: offset {} beyond end (capacity={})", offset, bytes.length);
			return new byte[0];
		}

		byte[] result = new byte[len];
		System.arraycopy(bytes, offset, result, 0, len);
		return result;
	}
}
