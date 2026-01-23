package awildgoose.gooseboy.gpu.vertex;

import java.util.HashMap;
import java.util.Map;

public enum PrimitiveType {
	TRIANGLES(0),
	QUADS(1);

	private static final Map<Integer, PrimitiveType> ID_MAP = new HashMap<>();

	static {
		for (PrimitiveType cmd : values()) {
			ID_MAP.put(cmd.id(), cmd);
		}
	}

	final int id;

	PrimitiveType(int id) {
		this.id = id;
	}

	public static PrimitiveType findPrimitiveById(int id) {
		PrimitiveType cmd = ID_MAP.get(id);
		if (cmd == null) throw new RuntimeException("Unknown GPU command with id: " + id);
		return cmd;
	}

	public int id() {
		return id;
	}
}
