package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
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

	public WasmCrate(Instance instance, String name) {
		this.instance = instance;
		this.name = name;
		this.permissions = this.loadPermissions();
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
		ProfilerFiller profilerFiller = Profiler.get();
		long now = System.nanoTime();
		profilerFiller.push("wasm");
		this.updateFunction.apply(now);
		profilerFiller.pop();
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
	}
}
