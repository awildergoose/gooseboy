package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.Gooseboy;

public class GpuMemoryReader implements MemoryReadConsumer {
	private final byte[] bytes;
	private final boolean canLog;

	public GpuMemoryReader(byte[] bytes, boolean canLog) {
		this.bytes = bytes;
		this.canLog = canLog;
	}

	private void warn(String format, Object... arguments) {
		if (this.canLog)
			Gooseboy.LOGGER.warn(format, arguments);
	}

	@Override
	public byte readByte(int offset) {
		return readBytes(offset, 1)[0];
	}

	@Override
	public int readInt(int offset) {
		if (offset < 0 || (long) offset + 4 > bytes.length) {
			this.warn("GpuMemoryReader.readInt: offset {} beyond end (capacity={})", offset, bytes.length);
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
			this.warn("GpuMemoryReader.readBytes: offset {} beyond end (capacity={})", offset, bytes.length);
			return new byte[len];
		}

		byte[] result = new byte[len];
		System.arraycopy(bytes, offset, result, 0, len);
		return result;
	}
}
