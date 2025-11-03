package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.WasmCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("input")
public final class Input {
	public Input() {}

	private static final int FALSE = 0;
	private static final int TRUE = 1;

	@WasmExport
	public int get_key(Instance instance, int key) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_KEYBOARD))
			return FALSE;
		return Gooseboy.ccb.isKeyDown(key) ? TRUE : FALSE;
	}

	@WasmExport
	public int get_mouse_button(Instance instance, int button) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE))
			return FALSE;
		return Gooseboy.ccb.isMouseButtonDown(button) ? TRUE : FALSE;
	}

	@WasmExport
	public int get_mouse_x(Instance instance) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE_POS))
			return FALSE;
		return Gooseboy.ccb.getMouseXInFramebuffer();
	}

	@WasmExport
	public int get_mouse_y(Instance instance) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE_POS))
			return FALSE;
		return Gooseboy.ccb.getMouseYInFramebuffer();
	}

	public HostFunction[] toHostFunctions() {
		return Input_ModuleFactory.toHostFunctions(this);
	}
}
