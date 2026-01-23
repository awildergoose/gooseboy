package awildgoose.gooseboy.gpu.consumer;

import awildgoose.gooseboy.gpu.mesh.MeshRef;
import awildgoose.gooseboy.gpu.texture.TextureRef;
import org.jetbrains.annotations.Nullable;

public interface RenderConsumer {
	void mesh(MeshRef mesh);

	void vertex(float x, float y, float z, float u, float v);

	void texture(@Nullable TextureRef texture);
}
