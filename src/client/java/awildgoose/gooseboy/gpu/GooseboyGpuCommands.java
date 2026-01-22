package awildgoose.gooseboy.gpu;

import java.util.ArrayList;

public class GooseboyGpuCommands {
	public static ArrayList<MeshRegistry.MeshRef> recordings = new ArrayList<>();

	public static void runCommand(GooseboyGpu.GpuCommand command, GooseboyGpu.MemoryReadOffsetConsumer read,
								  RenderConsumer render) {
		switch (command) {
			case Push, Pop, RegisterTexture, BindTexture -> {
				// TODO
			}
			case PushRecord -> recordings.add(MeshRegistry.createMesh());
			case PopRecord -> recordings.removeLast();
			case DrawRecorded -> {
				int id = read.readInt(1);
				render.mesh(MeshRegistry.getMesh(id));
			}
			case EmitVertex -> {
				MeshRegistry.MeshRef recording = recordings.getLast();

				if (recording == null) {
					// TODO handle this properly
				} else {
					float x = read.readFloat(1);
					float y = read.readFloat(5);
					float z = read.readFloat(9);
					float u = read.readFloat(13);
					float v = read.readFloat(17);

					recording.stack()
							.push(new VertexStack.Vertex(
									x, y, z, u, v
							));
				}
			}
		}
	}

	public interface RenderConsumer {
		void mesh(MeshRegistry.MeshRef mesh);
	}
}
