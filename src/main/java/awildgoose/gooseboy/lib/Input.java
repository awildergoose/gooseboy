package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateUtils;
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
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_KEYBOARD))
			return 0;
		return Gooseboy.ccb.isKeyDown(key) ? 1 : 0;
	}

	@WasmExport
	public int get_mouse_button(Instance instance, int button) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_MOUSE))
			return 0;
		return Gooseboy.ccb.isMouseButtonDown(button) ? 1 : 0;
	}

	@WasmExport
	public int get_mouse_x(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseXInFramebuffer();
	}

	@WasmExport
	public int get_mouse_y(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseYInFramebuffer();
	}

	@WasmExport
	public double get_mouse_accumulated_dx(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseAccumulatedDX();
	}

	@WasmExport
	public double get_mouse_accumulated_dy(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_MOUSE_POS))
			return 0;
		return Gooseboy.ccb.getMouseAccumulatedDY();
	}

	@WasmExport
	public int is_mouse_grabbed(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_GRAB_MOUSE))
			return 0;
		return Gooseboy.ccb.isMouseGrabbed() ? 1 : 0;
	}

	@WasmExport
	public int get_key_code(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_KEYBOARD))
			return -1;
		return Gooseboy.ccb.getKeyCode();
	}

	@WasmExport
	public void grab_mouse(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_GRAB_MOUSE))
			return;
		Gooseboy.ccb.grabMouse();
	}

	@WasmExport
	public void release_mouse(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.INPUT_GRAB_MOUSE))
			return;
		Gooseboy.ccb.releaseMouse();
	}

	public HostFunction[] toHostFunctions() {
		return Input_ModuleFactory.toHostFunctions(this);
	}
}
