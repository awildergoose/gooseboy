package awildgoose.gooseboy.gpu;

public class GooseboyGpu {
	public enum GpuCommand {
		Push(0x00, 0x00),
		Pop(0x01, 0x00),
		PushRecord(0x02, 0x00),
		PopRecord(0x03, 0x00),
		DrawRecorded(0x04, 0x04), // u32 id
		EmitVertex(0x05, 0x14), // f32 xyzuv[5]
		BindTexture(0x06, 0x04), // u32 id
		RegisterTexture(0x07, 0x08); // u32 w, u32 h, byte[] rgba

		private final int id;
		private final int len;

		GpuCommand(int id, int len) {
			this.id = id;
			this.len = len;
		}

		public static GpuCommand findCommandById(int id) {
			if (id == Push.id()) return Push;
			if (id == Pop.id()) return Pop;
			if (id == PushRecord.id()) return PushRecord;
			if (id == PopRecord.id()) return PopRecord;
			if (id == DrawRecorded.id()) return DrawRecorded;
			if (id == EmitVertex.id()) return EmitVertex;
			if (id == BindTexture.id()) return BindTexture;
			if (id == RegisterTexture.id()) return RegisterTexture;

			throw new RuntimeException("Unknown GPU command with id: " + id);
		}

		public int id() {
			return id;
		}

		public int len() {
			return len;
		}
	}

	public interface MemoryReadOffsetConsumer {
		int readInt(int offset);

		float readFloat(int offset);

		byte[] readBytes(int offset, int len);
	}

	public interface MemoryWriteOffsetConsumer {
		void writeInt(int offset, int value);

		void writeFloat(int offset, float value);

		void writeBytes(int offset, byte[] value);
	}

	public interface RenderConsumer {
		void mesh(MeshRegistry.MeshRef mesh);

		void vertex(float x, float y, float z, float u, float v);

		void texture(TextureRegistry.TextureRef texture);
	}

	public static final class QueuedCommand {
		private final GpuCommand command;
		private final GooseboyGpuMemoryReader reader;

		public QueuedCommand(GpuCommand command, byte[] payload) {
			this.command = command;
			this.reader = new GooseboyGpuMemoryReader(payload);
		}

		public GpuCommand command() {
			return command;
		}

		public GooseboyGpuMemoryReader reader() {
			return reader;
		}
	}
}
