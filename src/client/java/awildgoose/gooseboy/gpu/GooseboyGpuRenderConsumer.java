package awildgoose.gooseboy.gpu;

public class GooseboyGpuRenderConsumer implements GooseboyGpu.RenderConsumer {
	private final GooseboyGpuRenderer renderer;

	public GooseboyGpuRenderConsumer(GooseboyGpuRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void mesh(MeshRegistry.MeshRef mesh) {
		this.renderer.renderMesh(mesh);
	}

	@Override
	public void vertex(float x, float y, float z, float u, float v) {
		this.renderer.globalVertexStack.push(new VertexStack.Vertex(x, y, z, u, v));
	}

	@Override
	public void texture(TextureRegistry.TextureRef texture) {
		this.renderer.boundTexture = texture == null ? null : texture.texture;
	}
}
