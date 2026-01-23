package awildgoose.gooseboy.gpu.command;

import java.util.HashMap;
import java.util.Map;

public enum GpuCommand {
	Push(0x00, 0),
	Pop(0x01, 0),
	PushRecord(0x02, 1), // u8 primitiveType
	PopRecord(0x03, 0),
	DrawRecorded(0x04, 4), // u32 id
	EmitVertex(0x05, 20), // f32 xyzuv[5]
	BindTexture(0x06, 4), // u32 id
	RegisterTexture(0x07, 8), // u32 w, u32 h, byte[] rgba

	Translate(0x08, 12),
	RotateAxis(0x09, 16),
	RotateEuler(0x0A, 12),
	Scale(0x0B, 12),
	LoadMatrix(0x0C, 64),
	MulMatrix(0x0D, 64),
	Identity(0x0E, 0);

	private static final Map<Integer, GpuCommand> ID_MAP = new HashMap<>();

	static {
		for (GpuCommand cmd : values()) {
			ID_MAP.put(cmd.id(), cmd);
		}
	}

	private final int id;
	private final int len;

	GpuCommand(int id, int len) {
		this.id = id;
		this.len = len;
	}

	public static GpuCommand findCommandById(int id) {
		GpuCommand cmd = ID_MAP.get(id);
		if (cmd == null) throw new RuntimeException("Unknown GPU command with id: " + id);
		return cmd;
	}

	public int id() {
		return id;
	}

	public int len() {
		return len;
	}
}
