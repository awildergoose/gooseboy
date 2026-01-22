package awildgoose.gooseboy.gpu;

import com.dylibso.chicory.runtime.Memory;

public class GooseboyGpuMemoryReader implements GooseboyGpu.MemoryReadOffsetConsumer {
	private final Memory memory;
	private final int baseOffset;

	public GooseboyGpuMemoryReader(Memory memory, int baseOffset) {
		this.memory = memory;
		this.baseOffset = baseOffset;
	}

	@Override
	public int readInt(int offset) {
		return memory.readInt(baseOffset + offset);
	}

	@Override
	public float readFloat(int offset) {
		return memory.readFloat(baseOffset + offset);
	}

	public byte readByte(int offset) {
		return memory.read(baseOffset + offset);
	}

	public byte[] readBytes(int offset, int len) {
		return memory.readBytes(baseOffset + offset, len);
	}
}

