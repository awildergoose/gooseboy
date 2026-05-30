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
		return this.readBytes(offset, 1)[0];
	}

	@Override
	public int readInt(int offset) {
		if (offset < 0 || (long) offset + 4 > this.bytes.length) {
			this.warn("GpuMemoryReader.readInt: offset {} beyond end (capacity={})", offset, this.bytes.length);
			return 0;
		}

		return (this.bytes[offset] & 0xFF) |
				((this.bytes[offset + 1] & 0xFF) << 8) |
				((this.bytes[offset + 2] & 0xFF) << 16) |
				((this.bytes[offset + 3] & 0xFF) << 24);
	}

	@Override
	public float readFloat(int offset) {
		return Float.intBitsToFloat(this.readInt(offset));
	}

	@Override
	public byte[] readBytes(int offset, int len) {
		long end = (long) offset + (long) len;

		if (end > this.bytes.length) {
			this.warn("GpuMemoryReader.readBytes: offset {} beyond end (capacity={})", offset, this.bytes.length);
			return new byte[len];
		}

		byte[] result = new byte[len];
		System.arraycopy(this.bytes, offset, result, 0, len);
		return result;
	}
}
