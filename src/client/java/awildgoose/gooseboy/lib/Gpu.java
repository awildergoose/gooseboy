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

			// TODO improve this
			if (cmd.id() == GooseboyGpu.GpuCommand.RegisterTexture.id()) {
				// width * height * 4
				int width = memory.readInt(offset + 1);
				int height = memory.readInt(offset + 1 + 4);

				payloadLength += width * height * 4;
			}

			byte[] payload = memory.readBytes(offset + 1, payloadLength);

			gpu.queuedCommands.add(
					new GooseboyGpu.QueuedCommand(cmd, payload)
			);

			offset += 1 + payloadLength;
		}
	}

	public HostFunction[] toHostFunctions() {
		return Gpu_ModuleFactory.toHostFunctions(this);
	}
}
