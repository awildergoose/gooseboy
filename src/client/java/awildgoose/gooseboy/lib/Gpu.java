package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.gpu.GooseboyGpu;
import awildgoose.gooseboy.gpu.GooseboyGpuCamera;
import awildgoose.gooseboy.gpu.GooseboyGpuRenderer;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@HostModule("gpu")
public final class Gpu {
	public Gpu() {
	}

	@WasmExport
	public void get_camera_transform(Instance instance, int ptr) {
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return;

		GooseboyGpuCamera camera = gpu.camera;
		float[] values = {camera.getX(), camera.getY(), camera.getZ(), camera.getYaw(), camera.getPitch()};
		ByteBuffer transform = ByteBuffer.allocate(values.length * Float.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN);
		for (float value : values)
			transform.putFloat(value);
		instance.memory()
				.write(ptr, transform.array());
	}

	@WasmExport
	public void set_camera_transform(Instance instance, float x, float y, float z, float yaw, float pitch) {
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return;
		gpu.camera.setPosition(x, y, z);
		gpu.camera.rotation.set(yaw, pitch);
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
			GooseboyGpu.GpuCommand cmd = GooseboyGpu.GpuCommand.findCommandById(cmdId);

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

	@WasmExport
	public int gpu_read(Instance instance, int offset, int ptr, int len) {
		Memory memory = instance.memory();
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return 0;

		int toRead = Math.min(len, gpu.gpuMemory.capacity());
		byte[] chunk = new byte[toRead];
		gpu.gpuMemory.position(offset)
				.get(chunk, 0, toRead)
				.position(0);
		memory.write(ptr, chunk);

		return toRead;
	}

	public HostFunction[] toHostFunctions() {
		return Gpu_ModuleFactory.toHostFunctions(this);
	}
}
