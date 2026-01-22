package awildgoose.gooseboy.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VertexBuffer {
	private static final int VERTEX_SIZE = 5 * Float.BYTES; // 3 pos + 2 uv
	private ByteBuffer buffer;
	private int vertexCount = 0;

	public VertexBuffer(int initialVertices) {
		buffer = ByteBuffer.allocateDirect(initialVertices * VERTEX_SIZE)
				.order(ByteOrder.nativeOrder());
	}

	public int size() {
		return vertexCount;
	}

	private void ensureCapacity() {
		int required = (vertexCount + 1) * VERTEX_SIZE;
		if (required > buffer.capacity()) {
			int newCapacity = Math.max(required, buffer.capacity() * 2);
			ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity)
					.order(ByteOrder.nativeOrder());
			buffer.flip();
			newBuffer.put(buffer);
			buffer = newBuffer;
		}
	}

	public void put(float x, float y, float z, float u, float v) {
		ensureCapacity();
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(z);
		buffer.putFloat(u);
		buffer.putFloat(v);
		vertexCount++;
	}

	public void clear() {
		buffer.clear();
		vertexCount = 0;
	}

	public void forEachVertex(VertexIteratorConsumer consumer) {
		for (int i = 0; i < vertexCount; i++) {
			int pos = i * VERTEX_SIZE;
			float x = buffer.getFloat(pos);
			float y = buffer.getFloat(pos + 4);
			float z = buffer.getFloat(pos + 8);
			float u = buffer.getFloat(pos + 12);
			float v = buffer.getFloat(pos + 16);
			consumer.accept(x, y, z, u, v);
		}
	}

	@FunctionalInterface
	public interface VertexIteratorConsumer {
		void accept(float x, float y, float z, float u, float v);
	}
}
