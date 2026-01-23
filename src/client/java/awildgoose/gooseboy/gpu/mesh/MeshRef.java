package awildgoose.gooseboy.gpu.mesh;

import awildgoose.gooseboy.gpu.texture.TextureRef;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import org.jetbrains.annotations.Nullable;

public final class MeshRef {
	private final VertexStack stack;
	private final int id;
	public @Nullable TextureRef texture;

	public MeshRef(VertexStack stack, int id, @Nullable TextureRef texture) {
		this.stack = stack;
		this.id = id;
		this.texture = texture;
	}

	public VertexStack stack() {
		return stack;
	}

	public int id() {
		return id;
	}
}
