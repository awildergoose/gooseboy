package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.gpu.mesh.MeshRef;

@FunctionalInterface
public interface RenderConsumer {
	void mesh(MeshRef mesh);
}
