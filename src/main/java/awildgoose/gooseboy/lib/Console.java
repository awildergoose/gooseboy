package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateUtils;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("console")
public final class Console {
	public Console() {}

	@WasmExport
	public void log(Instance instance, int ptr, int len) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.CONSOLE))
			return;

		String text = instance.memory()
				.readString(ptr, len);
		String name = Gooseboy.getCrate(instance).name;

		if (text.startsWith("PANIC at ")) {
			Gooseboy.ccb.doErrorMessage("Crate panic", text);
			Gooseboy.LOGGER.error("[{}] {}", name, text);
		} else {
			Gooseboy.LOGGER.info("[{}] {}", name, text);
		}
	}

	public HostFunction[] toHostFunctions() {
		return Console_ModuleFactory.toHostFunctions(this);
	}
}
