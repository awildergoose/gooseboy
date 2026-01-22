package awildgoose.gooseboy.gpu;

public class GooseboyGpuRenderConsumer implements GooseboyGpuCommands.RenderConsumer {
	private final GooseboyGpuRenderer renderer;

	public GooseboyGpuRenderConsumer(GooseboyGpuRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void mesh(MeshRegistry.MeshRef mesh) {
		this.renderer.renderMesh(mesh);
	}
}
