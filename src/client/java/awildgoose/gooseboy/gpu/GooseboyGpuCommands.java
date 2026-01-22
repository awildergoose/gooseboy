package awildgoose.gooseboy.gpu;

import java.util.ArrayList;

public class GooseboyGpuCommands {
	public static ArrayList<MeshRegistry.MeshRef> recordings = new ArrayList<>();

	public static void runCommand(GooseboyGpu.GpuCommand command, GooseboyGpu.MemoryReadOffsetConsumer read,
								  RenderConsumer render) {
		switch (command) {
			case Push, Pop -> {
				// TODO
			}
			case PushRecord -> recordings.add(MeshRegistry.createMesh());
			case PopRecord -> recordings.removeLast();
			case DrawRecorded -> render.mesh(MeshRegistry.getMesh(read.readInt(0)));
			case EmitVertex -> {
				MeshRegistry.MeshRef recording = recordings.getLast();
				float x = read.readFloat(0);
				float y = read.readFloat(4);
				float z = read.readFloat(8);
				float u = read.readFloat(12);
				float v = read.readFloat(16);

				if (recording == null) {
					// immediate-mode
					render.vertex(x, y, z, u, v);
				} else {
					// recommended instanced mode
					recording.stack()
							.push(new VertexStack.Vertex(
									x, y, z, u, v
							));
				}
			}
			case RegisterTexture -> {
				int ptr = read.readInt(0);
				int width = read.readInt(4);
				int height = read.readInt(8);
				TextureRegistry.TextureRef texture = TextureRegistry.createTexture(width, height);
				texture.set(read, ptr, width * height * 4);
			}
			case BindTexture -> render.texture(TextureRegistry.getTexture(read.readInt(0)));
		}
	}

	public interface RenderConsumer {
		void mesh(MeshRegistry.MeshRef mesh);

		void vertex(float x, float y, float z, float u, float v);

		void texture(TextureRegistry.TextureRef texture);
	}
}
