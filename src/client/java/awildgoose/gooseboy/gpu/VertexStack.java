package awildgoose.gooseboy.gpu;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public final class VertexStack {
	private final ArrayList<Vertex> vertices = new ArrayList<>();
	private GpuBuffer gpuBuffer;

	public VertexStack() {
	}

	public VertexStack push(Vertex vertex) {
		this.vertices.add(vertex);
		return this;
	}

	public void flush() {
		this.vertices.clear();
	}

	public void build(PoseStack.Pose pose, VertexConsumer consumer) {
		for (Vertex vertex : this.vertices)
			consumer.addVertex(pose, vertex.pos)
					.setUv(vertex.uv.x, vertex.uv.y);
	}

	public GpuBuffer intoGpuBuffer(PoseStack.Pose pose) {
		final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_TEX;
		int len = VERTEX_FORMAT.getVertexSize() * this.vertices.size();

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
			this.build(pose, buffer);

			try (MeshData meshData = buffer.buildOrThrow()) {
				RenderSystem.getDevice()
						.createCommandEncoder()
						.writeToBuffer(
								gpuBuffer.slice(), meshData.vertexBuffer());
			}
		}

		return gpuBuffer;
	}

	public record Vertex(Vector3f pos, Vector2f uv) {
	}
}
