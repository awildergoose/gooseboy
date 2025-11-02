package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Memory;

@HostModule("console")
public final class Console {
	public Console() {}

	@WasmExport
	public void log(Memory memory, int ptr, int len) {
		Gooseboy.LOGGER.info(memory.readString(ptr, len));
	}

	public HostFunction[] toHostFunctions() {
		return Console_ModuleFactory.toHostFunctions(this);
	}
}
