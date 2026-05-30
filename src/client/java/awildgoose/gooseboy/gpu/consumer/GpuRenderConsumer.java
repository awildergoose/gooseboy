package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.gpu.mesh.MeshRef;
import awildgoose.gooseboy.gpu.render.GooseboyGpuRenderer;

public class GpuRenderConsumer implements RenderConsumer {
	private final GooseboyGpuRenderer renderer;

	public GpuRenderConsumer(GooseboyGpuRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void mesh(MeshRef mesh) {
		this.renderer.renderMesh(mesh);
	}
}
