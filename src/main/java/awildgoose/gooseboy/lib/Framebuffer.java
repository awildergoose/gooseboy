package awildgoose.gooseboy.lib;


import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;

@HostModule("fb")
public final class Framebuffer {
	public Framebuffer() {}

	@WasmExport
	public int get_framebuffer_width() {
		return Gooseboy.FRAMEBUFFER_WIDTH;
	}

	@WasmExport
	public int get_framebuffer_height() {
		return Gooseboy.FRAMEBUFFER_HEIGHT;
	}

	@WasmExport
	public void clear(int color) {
		Gooseboy.ccb.clear(color);
	}

	public HostFunction[] toHostFunctions() {
		return Framebuffer_ModuleFactory.toHostFunctions(this);
	}
}
