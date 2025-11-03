package awildgoose.gooseboy.lib;

import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Memory;

@HostModule("memory")
public final class Mem {
	public Mem() {}

	@WasmExport
	public void mem_fill(Memory memory, int address, int len, int value) {
		for (int i = 0; i < len; i += 4) {
			memory.writeI32(address + i, value);
		}
	}

	@WasmExport
	public void mem_copy(Memory memory, int dst, int src, int len) {
		memory.copy(dst, src, len);
	}

	public HostFunction[] toHostFunctions() {
		return Mem_ModuleFactory.toHostFunctions(this);
	}
}
