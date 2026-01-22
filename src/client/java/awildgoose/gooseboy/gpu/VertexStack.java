package awildgoose.gooseboy.gpu;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.Nullable;

public final class VertexStack {
	private final VertexBuffer vertices = new VertexBuffer(0);
	private GpuBuffer gpuBuffer;

	public VertexStack() {
	}

	public int size() {
		return this.vertices.size();
	}

	public VertexStack push(Vertex vertex) {
		this.vertices.put(vertex.x, vertex.y, vertex.z, vertex.u, vertex.v);
		return this;
	}

	public void clear() {
		this.vertices.clear();
	}

	public void build(VertexConsumer consumer) {
		this.vertices.forEachVertex((x, y, z, u, v) -> consumer.addVertex(x, y, z)
				.setUv(u, v));
	}

	public @Nullable GpuBuffer intoGpuBuffer() {
		final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_TEX;
		int len = VERTEX_FORMAT.getVertexSize() * this.vertices.size();

		if (len == 0) {
			// no vertices
			return null;
		}

		if (gpuBuffer == null || gpuBuffer.size() < len) {
			if (gpuBuffer != null) gpuBuffer.close();
			gpuBuffer = RenderSystem.getDevice()
					.createBuffer(
							null,
							GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
							len
					);
		}

		try (ByteBufferBuilder byteBuffer = ByteBufferBuilder.exactlySized(len)) {
			BufferBuilder buffer = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, VERTEX_FORMAT);
			this.build(buffer);

			try (MeshData meshData = buffer.buildOrThrow()) {
				RenderSystem.getDevice()
						.createCommandEncoder()
						.writeToBuffer(
								gpuBuffer.slice(), meshData.vertexBuffer());
			}
		}

		return gpuBuffer;
	}

	public record Vertex(float x, float y, float z, float u, float v) {
	}
}
