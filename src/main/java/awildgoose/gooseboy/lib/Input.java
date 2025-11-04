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

	@WasmExport
	public int get_key(Instance instance, int key) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_KEYBOARD))
			return 0;
		return Gooseboy.ccb.isKeyDown(key) ? 1 : 0;
	}

	@WasmExport
	public int get_mouse_button(Instance instance, int button) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE))
			return 0;
		return Gooseboy.ccb.isMouseButtonDown(button) ? 1 : 0;
	}

	@WasmExport
	public int get_mouse_x(Instance instance) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseXInFramebuffer();
	}

	@WasmExport
	public int get_mouse_y(Instance instance) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseYInFramebuffer();
	}

	@WasmExport
	public int get_key_code(Instance instance) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.INPUT_KEYBOARD))
			return -1;
		return Gooseboy.ccb.getKeyCode();
	}

	public HostFunction[] toHostFunctions() {
		return Input_ModuleFactory.toHostFunctions(this);
	}
}
