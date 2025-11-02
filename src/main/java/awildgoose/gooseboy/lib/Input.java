package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;

@HostModule("input")
public final class Input {
	public Input() {}

	private static final int FALSE = 0;
	private static final int TRUE = 1;

	@WasmExport
	public int get_key(int key) {
		return Gooseboy.ccb.isKeyDown(key) ? TRUE : FALSE;
	}

	@WasmExport
	public int get_mouse_button(int button) {
		return Gooseboy.ccb.isMouseButtonDown(button) ? TRUE : FALSE;
	}

	@WasmExport
	public int get_mouse_x() {
		return Gooseboy.ccb.getMouseXInFramebuffer();
	}

	@WasmExport
	public int get_mouse_y() {
		return Gooseboy.ccb.getMouseYInFramebuffer();
	}

	public HostFunction[] toHostFunctions() {
		return Input_ModuleFactory.toHostFunctions(this);
	}
}
