package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.TrapException;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class WasmCrate {
	public final Instance instance;
	private int fbPtr;
	public int fbSize;
	private ExportFunction updateFunction;
	public CrateStorage storage;
	public final String name;
	public final List<Permission> permissions;
	public boolean isOk = true;

	public WasmCrate(Instance instance, String name) {
		this.instance = instance;
		this.name = name;
		this.permissions = this.loadPermissions();
		this.init();
	}

	private void init() {
		this.fbSize = FRAMEBUFFER_WIDTH * FRAMEBUFFER_HEIGHT * 4;
		this.storage = new CrateStorage(this.name);
		Gooseboy.addCrate(this);

		try {
			instance.export("main").apply();
			this.fbPtr = (int) this.instance.export("get_framebuffer_ptr").apply()[0];
			this.updateFunction = this.instance.export("update");
		} catch (Throwable ie) {
			this.close();
			if (ie instanceof TrapException) {
				this.isOk = false;
				ie.printStackTrace();
			} else
				throw ie;
		}

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
		if (this.updateFunction != null) {
			ProfilerFiller profilerFiller = Profiler.get();
			long now = System.nanoTime();
			profilerFiller.push("wasm");
			try {
				this.updateFunction.apply(now);
			} catch (TrapException e) {
				this.isOk = false;
				e.printStackTrace();
			}
			profilerFiller.pop();
		}
	}

	public void close() {
		this.savePermissions();
		this.storage.save();
		Gooseboy.removeCrate(this);
	}

	private List<Permission> loadPermissions() {
		return ConfigManager.getEffectivePermissions(this.name);
	}

	private void savePermissions() {
		ConfigManager.setCratePermissions(this.name, this.permissions);
	}

	public enum Permission {
		CONSOLE,
		AUDIO,
		INPUT_KEYBOARD,
		INPUT_MOUSE,
		INPUT_MOUSE_POS,
		STORAGE_READ,
		STORAGE_WRITE,
		EXTENDED_MEMORY
	}
}
