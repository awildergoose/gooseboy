package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.gpu.GooseboyGpu;
import awildgoose.gooseboy.gpu.GooseboyGpuMemoryReader;
import awildgoose.gooseboy.gpu.GooseboyGpuRenderConsumer;
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
	public void submit_gpu_commands(Instance instance, int ptr, int len) {
		int offset = ptr;

		Memory memory = instance.memory();
		GooseboyGpuRenderConsumer renderConsumer = new GooseboyGpuRenderConsumer(
				GooseboyClient.rendererByInstance.get(instance)
		);

		while (offset < len) {
			byte cmdId = memory.read(offset);
			GooseboyGpu.GpuCommand cmd = GooseboyGpu.findCommandById(cmdId);

			GooseboyGpuMemoryReader reader = new GooseboyGpuMemoryReader(memory, offset);
			GooseboyGpu.commandRunner.runCommand(cmd, reader, renderConsumer);

			offset += 1 + cmd.len();
		}
	}

	public HostFunction[] toHostFunctions() {
		return Gpu_ModuleFactory.toHostFunctions(this);
	}
}
