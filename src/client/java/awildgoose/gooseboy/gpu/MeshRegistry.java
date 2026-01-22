package awildgoose.gooseboy.gpu;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class MeshRegistry {
	private static final ArrayList<MeshRef> meshes = new ArrayList<>();
	private static int lastMeshId = 0;

	public static MeshRef createMesh() {
		MeshRef ref = new MeshRef(new VertexStack(), lastMeshId++);
		meshes.add(ref);
		return ref;
	}

	public static @Nullable MeshRef getMesh(int id) {
		Optional<MeshRef> ref = meshes.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.orElse(null);
	}

	public record MeshRef(VertexStack stack, int id) {
	}
}
