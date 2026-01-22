package awildgoose.gooseboy.gpu;

import com.dylibso.chicory.runtime.Memory;

public class GooseboyGpu {
	public enum GpuCommand {
		Push(0x00, 0x00),
		Pop(0x01, 0x00),
		PushRecord(0x02, 0x00),
		PopRecord(0x03, 0x00),
		DrawRecorded(0x04, 0x04), // u32 id
		EmitVertex(0x05, 0x14), // f32 xyzuv[5]
		BindTexture(0x06, 0x04), // u32 id
		RegisterTexture(0x07, 0xC); // u32 ptr, u32 w, u32 h

		private final int id;
		private final int len;

		GpuCommand(int id, int len) {
			this.id = id;
			this.len = len;
		}

		public int id() {
			return id;
		}

		public int len() {
			return len;
		}
	}

	public static GpuCommand findCommandById(int id) {
		if (id == GpuCommand.Push.id()) return GpuCommand.Push;
		if (id == GpuCommand.Pop.id()) return GpuCommand.Pop;
		if (id == GpuCommand.PushRecord.id()) return GpuCommand.PushRecord;
		if (id == GpuCommand.PopRecord.id()) return GpuCommand.PopRecord;
		if (id == GpuCommand.DrawRecorded.id()) return GpuCommand.DrawRecorded;
		if (id == GpuCommand.EmitVertex.id()) return GpuCommand.EmitVertex;
		if (id == GpuCommand.BindTexture.id()) return GpuCommand.BindTexture;
		if (id == GpuCommand.RegisterTexture.id()) return GpuCommand.RegisterTexture;

		throw new RuntimeException("Unknown GPU command with id: " + id);
	}

	public interface MemoryReadOffsetConsumer {
		int readInt(int offset);

		float readFloat(int offset);

		byte readByte(int offset);

		byte[] readBytes(int offset, int len);

		byte[] readGlobalBytes(int ptr, int len);
	}

	public static final class QueuedCommand {
		private final GooseboyGpu.GpuCommand command;
		private final GooseboyGpuMemoryReader reader;

		public QueuedCommand(GooseboyGpu.GpuCommand command,
							 Memory memory,
							 int payloadBase) {
			this.command = command;
			this.reader = new GooseboyGpuMemoryReader(memory, payloadBase);
		}

		public GooseboyGpu.GpuCommand command() {
			return command;
		}

		public GooseboyGpuMemoryReader reader() {
			return reader;
		}
	}
}
