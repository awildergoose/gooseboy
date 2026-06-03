package awildgoose.gooseboy.gpu.mesh;

import awildgoose.gooseboy.gpu.vertex.PrimitiveType;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class MeshRegistry {
	private final ArrayList<MeshRef> meshes = new ArrayList<>();
	private int lastMeshId = 0;

	public void close() {
		for (MeshRef mesh : this.meshes)
			mesh.stack()
					.clear();
		this.meshes.clear();
	}

	public MeshRef createMesh(PrimitiveType primitiveType) {
		MeshRef ref = new MeshRef(new VertexStack(), primitiveType, this.lastMeshId++, null);
		this.meshes.add(ref);
		return ref;
	}

	public @Nullable MeshRef getMesh(int id) {
		Optional<MeshRef> ref = this.meshes.stream()
				.filter(f -> f.id() == id)
				.findFirst();
		return ref.orElse(null);
	}
}
