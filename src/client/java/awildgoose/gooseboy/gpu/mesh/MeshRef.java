package awildgoose.gooseboy.gpu.mesh;

import awildgoose.gooseboy.gpu.texture.TextureRef;
import awildgoose.gooseboy.gpu.vertex.PrimitiveType;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import org.jetbrains.annotations.Nullable;

public final class MeshRef {
	private final VertexStack stack;
	private final int id;
	private final PrimitiveType primitiveType;
	public @Nullable TextureRef texture;

	public MeshRef(VertexStack stack, PrimitiveType primitiveType, int id, @Nullable TextureRef texture) {
		this.stack = stack;
		this.primitiveType = primitiveType;
		this.id = id;
		this.texture = texture;
	}

	public VertexStack stack() {
		return stack;
	}

	public int id() {
		return id;
	}

	public PrimitiveType primitiveType() {
		return primitiveType;
	}
}
