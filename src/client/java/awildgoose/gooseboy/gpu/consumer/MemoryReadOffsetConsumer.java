package awildgoose.gooseboy.gpu.consumer;

public interface MemoryReadOffsetConsumer {
	int readInt(int offset);

	float readFloat(int offset);

	byte[] readBytes(int offset, int len);
}
