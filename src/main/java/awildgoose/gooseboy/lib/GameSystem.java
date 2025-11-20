package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

import java.util.Optional;

@HostModule("system")
public final class GameSystem {
	public GameSystem() {
	}

	@WasmExport
	public long get_time_nanos() {
		return System.nanoTime();
	}

	@WasmExport
	public int has_permission(Instance instance, int permission) {
		Optional<GooseboyCrate.Permission> permissionEnum = GooseboyCrate.Permission.intToEnum(permission);
		return permissionEnum.filter(value -> Gooseboy.getCrate(instance).permissions.contains(value))
				.map(value -> 1)
				.orElse(0);
	}

	public HostFunction[] toHostFunctions() {
		return GameSystem_ModuleFactory.toHostFunctions(this);
	}
}
