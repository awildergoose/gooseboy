package awildgoose.gooseboy.gpu;

import java.nio.ByteBuffer;

public class GooseboyGpuMemoryConsumer implements GooseboyGpu.MemoryWriteOffsetConsumer {
	private final ByteBuffer buffer;

	public GooseboyGpuMemoryConsumer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void writeInt(int offset, int value) {
		this.buffer.putInt(offset, value);
	}

	@Override
	public void writeFloat(int offset, float value) {
		this.buffer.putFloat(offset, value);
	}

	@Override
	public void writeBytes(int offset, byte[] value) {
		this.buffer.put(offset, value);
	}
}
