package awildgoose.gooseboy.gpu;

import com.dylibso.chicory.runtime.Memory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
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

	public static @Nullable VertexStack getStack(int id) {
		Optional<MeshRef> ref = meshes.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.map(MeshRef::stack)
				.orElse(null);
	}

	public record MeshRef(VertexStack stack, int id) {
		public void set(Memory memory, int ptr, int len) {
			ByteBuffer vertices = ByteBuffer.wrap(memory.readBytes(ptr, len));
			MemoryUtil.memCopy(
					MemoryUtil.memAddress(vertices),
					this.stack.getPointer(),
					len);
		}
	}
}
