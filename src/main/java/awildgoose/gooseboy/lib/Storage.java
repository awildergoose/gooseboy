package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateUtils;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("storage")
public class Storage {
	public Storage() {}

	@WasmExport
	public int storage_read(Instance instance, int offset, int ptr, int len) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.STORAGE_READ))
			return 0;
		return Gooseboy.getCrate(instance).storage.read(instance.memory(), offset, ptr, len);
	}

	@WasmExport
	public int storage_write(Instance instance, int offset, int ptr, int len) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.STORAGE_WRITE))
			return 0;
		return Gooseboy.getCrate(instance).storage.write(instance.memory(), offset, ptr, len);
	}

	@WasmExport
	public int storage_size(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.STORAGE_READ))
			return 0;
		return Gooseboy.getCrate(instance).storage.size();
	}

	@WasmExport
	public void storage_clear(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.STORAGE_WRITE))
			return;
		Gooseboy.getCrate(instance).storage.clear();
	}

	public HostFunction[] toHostFunctions() {
		return Storage_ModuleFactory.toHostFunctions(this);
	}
}
