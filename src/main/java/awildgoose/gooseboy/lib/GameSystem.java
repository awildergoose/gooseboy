package awildgoose.gooseboy.lib;

import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;

@HostModule("system")
public final class GameSystem {
	public GameSystem() {}

	@WasmExport
	public long get_time_nanos() {
		return System.nanoTime();
	}

	public HostFunction[] toHostFunctions() {
		return GameSystem_ModuleFactory.toHostFunctions(this);
	}
}
