package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("storage")
public class Storage {
	public Storage() {}

	@WasmExport
	public int storage_read(Instance instance, int offset, int ptr, int len) {
		return Gooseboy.getCrate(instance).storage.read(instance.memory(), offset, ptr, len);
	}

	@WasmExport
	public int storage_write(Instance instance, int offset, int ptr, int len) {
		return Gooseboy.getCrate(instance).storage.write(instance.memory(), offset, ptr, len);
	}

	@WasmExport
	public int storage_size(Instance instance) {
		return Gooseboy.getCrate(instance).storage.size();
	}

	@WasmExport
	public void storage_clear(Instance instance) {
		Gooseboy.getCrate(instance).storage.clear();
	}

	public HostFunction[] toHostFunctions() {
		return Storage_ModuleFactory.toHostFunctions(this);
	}
}
