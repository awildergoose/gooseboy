package awildgoose.gooseboy.gpu;

import com.dylibso.chicory.runtime.Memory;

public class GooseboyGpuMemoryReader implements GooseboyGpu.MemoryReadOffsetConsumer {
	private final Memory memory;
	private final byte[] bytes;
	private final int baseOffset;

	public GooseboyGpuMemoryReader(byte[] bytes) {
		this.bytes = bytes;
		this.baseOffset = 0;
		this.memory = null;
	}

	@Override
	public int readInt(int offset) {
		if (memory != null) return memory.readInt(baseOffset + offset);
		return (bytes[baseOffset + offset] & 0xFF) |
				((bytes[baseOffset + offset + 1] & 0xFF) << 8) |
				((bytes[baseOffset + offset + 2] & 0xFF) << 16) |
				((bytes[baseOffset + offset + 3] & 0xFF) << 24);
	}

	@Override
	public float readFloat(int offset) {
		return Float.intBitsToFloat(readInt(offset));
	}

	public byte readByte(int offset) {
		if (memory != null) return memory.read(baseOffset + offset);
		return bytes[baseOffset + offset];
	}

	public byte[] readBytes(int offset, int len) {
		if (memory != null) return memory.readBytes(baseOffset + offset, len);
		byte[] result = new byte[len];
		System.arraycopy(bytes, baseOffset + offset, result, 0, len);
		return result;
	}
}
