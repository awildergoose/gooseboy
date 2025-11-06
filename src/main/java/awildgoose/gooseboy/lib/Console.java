package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateUtils;
import awildgoose.gooseboy.crate.WasmCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("console")
public final class Console {
	public Console() {}

	@WasmExport
	public void log(Instance instance, int ptr, int len) {
		if (CrateUtils.doesNotHavePermission(instance, WasmCrate.Permission.CONSOLE))
			return;
		Gooseboy.LOGGER.info("[{}] {}", Gooseboy.getCrate(instance).name, instance.memory().readString(ptr, len));
	}

	public HostFunction[] toHostFunctions() {
		return Console_ModuleFactory.toHostFunctions(this);
	}
}
