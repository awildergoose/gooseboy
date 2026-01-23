package awildgoose.gooseboy.gpu.consumer;

public interface MemoryWriteConsumer {
	void writeInt(int offset, int value);

	void writeFloat(int offset, float value);

	void writeBytes(int offset, byte[] value);
}
