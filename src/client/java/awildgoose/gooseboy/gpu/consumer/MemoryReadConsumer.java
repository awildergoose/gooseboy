package awildgoose.gooseboy.gpu.consumer;

public interface MemoryReadConsumer {
	byte readByte(int offset);

	int readInt(int offset);
	float readFloat(int offset);
	byte[] readBytes(int offset, int len);
}
