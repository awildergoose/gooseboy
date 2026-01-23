package awildgoose.gooseboy.gpu.consumer;

import java.nio.ByteBuffer;

public class GpuMemoryWriter implements MemoryWriteConsumer {
	private final ByteBuffer buffer;

	public GpuMemoryWriter(ByteBuffer buffer) {
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
