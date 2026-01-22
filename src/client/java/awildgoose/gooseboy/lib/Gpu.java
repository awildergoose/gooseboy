package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.gpu.GooseboyGpu;
import awildgoose.gooseboy.gpu.GooseboyGpuRenderer;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;

@HostModule("gpu")
public final class Gpu {
	public Gpu() {
	}

	@WasmExport
	public void submit_gpu_commands(Instance instance, int ptr, int count) {
		int offset = ptr;
		int end = ptr + count;

		Memory memory = instance.memory();
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return;

		while (offset < end) {
			byte cmdId = memory.read(offset);
			GooseboyGpu.GpuCommand cmd = GooseboyGpu.findCommandById(cmdId);

			int payloadLength = cmd.len();

			gpu.queuedCommands.add(
					new GooseboyGpu.QueuedCommand(cmd, memory, offset + 1)
			);

			offset += 1 + payloadLength;
		}
	}

	public HostFunction[] toHostFunctions() {
		return Gpu_ModuleFactory.toHostFunctions(this);
	}
}
