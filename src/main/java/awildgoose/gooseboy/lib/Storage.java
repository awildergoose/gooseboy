package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Memory;

@HostModule("storage")
public class Storage {
	public Storage() {}

	@WasmExport
	public int storage_read(Memory memory, int offset, int ptr, int len) {
		return Gooseboy.ccb.getStorageCrate().read(memory, offset, ptr, len);
	}

	@WasmExport
	public int storage_write(Memory memory, int offset, int ptr, int len) {
		return Gooseboy.ccb.getStorageCrate().write(memory, offset, ptr, len);
	}

	@WasmExport
	public int storage_size() {
		return Gooseboy.ccb.getStorageCrate().size();
	}

	@WasmExport
	public void storage_clear() {
		Gooseboy.ccb.getStorageCrate().clear();
	}

	public HostFunction[] toHostFunctions() {
		return Storage_ModuleFactory.toHostFunctions(this);
	}
}
