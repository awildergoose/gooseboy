package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;

import java.util.EnumSet;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class WasmCrate {
	public final Instance instance;
	private int fbPtr;
	public int fbSize;
	private ExportFunction updateFunction;
	public CrateStorage storage;
	public final String name;
	public EnumSet<Permission> permissions;

	public WasmCrate(Instance instance, String name) {
		this.instance = instance;
		this.name = name;
		this.permissions = EnumSet.noneOf(Permission.class);
		this.init();
	}

	private void init() {
		// TODO make sure the functions we're calling or exporting do exist!
		this.fbPtr = (int) this.instance.export("get_framebuffer_ptr").apply()[0];
		this.fbSize = FRAMEBUFFER_WIDTH * FRAMEBUFFER_HEIGHT * 4;
		this.updateFunction = this.instance.export("update");
		this.storage = new CrateStorage(this.name);
		Gooseboy.addCrate(this);
	}

	public void clearFramebuffer(int color) {
		var mem = this.instance.memory();
		int p = this.fbPtr;

		for (int i = 0; i < this.fbSize; i += 4) {
			mem.writeI32(p + i, color);
		}
	}

	public byte[] getFramebufferBytes() {
		return this.instance.memory().readBytes(this.fbPtr, this.fbSize);
	}

	public void update() {
		long now = System.nanoTime();
		this.updateFunction.apply(now);
	}

	public void close() {
		this.storage.save();
		Gooseboy.removeCrate(this);
	}

	public enum Permission {
		CONSOLE,
		AUDIO,
		INPUT_KEYBOARD,
		INPUT_MOUSE,
		INPUT_MOUSE_POS,
		STORAGE_READ,
		STORAGE_WRITE,
	}
}
