package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.gpu.GooseboyGpuRenderer;
import awildgoose.gooseboy.gpu.mesh.MeshRef;
import awildgoose.gooseboy.gpu.texture.TextureRef;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import org.jetbrains.annotations.Nullable;

public class GpuRenderConsumer implements RenderConsumer {
	private final GooseboyGpuRenderer renderer;

	public GpuRenderConsumer(GooseboyGpuRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void mesh(MeshRef mesh) {
		this.renderer.renderMesh(mesh);
	}

	@Override
	public void vertex(float x, float y, float z, float u, float v) {
		this.renderer.globalVertexStack.push(new VertexStack.Vertex(x, y, z, u, v));
	}

	@Override
	public void texture(@Nullable TextureRef texture) {
		this.renderer.boundTexture = texture == null ? null : texture.texture;
	}
}
