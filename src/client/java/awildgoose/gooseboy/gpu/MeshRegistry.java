package awildgoose.gooseboy.gpu;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class MeshRegistry {
	private final ArrayList<MeshRef> meshes = new ArrayList<>();
	private int lastMeshId = 0;

	public MeshRef createMesh() {
		MeshRef ref = new MeshRef(new VertexStack(), lastMeshId++, null);
		meshes.add(ref);
		return ref;
	}

	public @Nullable MeshRef getMesh(int id) {
		Optional<MeshRef> ref = meshes.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.orElse(null);
	}

	public static final class MeshRef {
		private final VertexStack stack;
		private final int id;
		public TextureRegistry.@Nullable TextureRef texture;

		public MeshRef(VertexStack stack, int id, @Nullable TextureRegistry.TextureRef texture) {
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
}
