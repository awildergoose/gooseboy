package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.TrapException;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

public class GooseboyCrate implements AutoCloseable {
	public final Instance instance;
	private int fbPtr;
	public int fbSize;
	private ExportFunction updateFunction;
	public CrateStorage storage;
	public final String name;
	public final List<Permission> permissions;
	public boolean isOk = true;

	public GooseboyCrate(Instance instance, String name, CrateMeta meta) {
		this.instance = instance;
		this.name = name;
		this.permissions = this.loadPermissions();
		this.init(meta);
	}

	private void init(CrateMeta meta) {
		this.fbSize = FRAMEBUFFER_WIDTH * FRAMEBUFFER_HEIGHT * 4;
		this.storage = new CrateStorage(this.name);

		// Free the binary, as we don't need it anymore
		meta.binary = null;
		Gooseboy.addCrate(this, meta);

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

	public void clearSurface(int ptr, int size, int color) {
		var mem = this.instance.memory();

		for (int i = 0; i < size; i += 4) {
			mem.writeI32(ptr + i, color);
		}
	}

	public byte[] getFramebufferBytes() {
		return this.instance.memory().readBytes(this.fbPtr, this.fbSize);
	}

	public void update() {
		if (this.updateFunction != null) {
			ProfilerFiller profilerFiller = Profiler.get();
			long now = System.nanoTime();
			profilerFiller.push("gooseboy");
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
		CrateUtils.clearWarns();
		Gooseboy.ccb.closeCrate(this);
		Gooseboy.removeCrate(this);
	}

	private List<Permission> loadPermissions() {
		return ConfigManager.getEffectivePermissions(this.name);
	}

	private void savePermissions() {
		ConfigManager.setCratePermissions(this.name, this.permissions);
	}

	public enum Permission {
		CONSOLE(0),
		AUDIO(1),
		INPUT_KEYBOARD(2),
		INPUT_MOUSE(3),
		INPUT_MOUSE_POS(4),
		INPUT_GRAB_MOUSE(5),
		STORAGE_READ(6),
		STORAGE_WRITE(7);

		private final int id;

		Permission(int id) {
			this.id = id;
		}

		public static Optional<Permission> intToEnum(int i) {
			return Arrays.stream(Permission.values()).filter(f -> f.id == i).findFirst();
		}
	}
}
