package awildgoose.gooseboy.gpu.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VertexBuffer {
	private static final int VERTEX_SIZE = 5 * Float.BYTES; // 3 pos + 2 uv
	private ByteBuffer buffer;
	private int vertexCount = 0;

	public VertexBuffer(int initialVertices) {
		this.buffer = ByteBuffer.allocateDirect(initialVertices * VERTEX_SIZE)
				.order(ByteOrder.nativeOrder());
	}

	public int size() {
		return this.vertexCount;
	}

	private void ensureCapacity() {
		int required = (this.vertexCount + 1) * VERTEX_SIZE;
		if (required > this.buffer.capacity()) {
			int newCapacity = Math.max(required, this.buffer.capacity() * 2);
			ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity)
					.order(ByteOrder.nativeOrder());
			this.buffer.flip();
			newBuffer.put(this.buffer);
			this.buffer = newBuffer;
		}
	}

	public void put(float x, float y, float z, float u, float v) {
		this.ensureCapacity();
		this.buffer.putFloat(x);
		this.buffer.putFloat(y);
		this.buffer.putFloat(z);
		this.buffer.putFloat(u);
		this.buffer.putFloat(v);
		this.vertexCount++;
	}

	public void clear() {
		this.buffer.clear();
		this.vertexCount = 0;
	}

	public void forEachVertex(VertexIteratorConsumer consumer) {
		for (int i = 0; i < this.vertexCount; i++) {
			int pos = i * VERTEX_SIZE;
			float x = this.buffer.getFloat(pos);
			float y = this.buffer.getFloat(pos + 4);
			float z = this.buffer.getFloat(pos + 8);
			float u = this.buffer.getFloat(pos + 12);
			float v = this.buffer.getFloat(pos + 16);
			consumer.accept(x, y, z, u, v);
		}
	}

	@FunctionalInterface
	public interface VertexIteratorConsumer {
		void accept(float x, float y, float z, float u, float v);
	}
}
