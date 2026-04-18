package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.crate.CrateUtils;
import awildgoose.gooseboy.crate.GooseboyCrate;
import awildgoose.gooseboy.gpu.command.GpuCommand;
import awildgoose.gooseboy.gpu.command.QueuedCommand;
import awildgoose.gooseboy.gpu.render.GooseboyGpuCamera;
import awildgoose.gooseboy.gpu.render.GooseboyGpuRenderer;
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
	public int get_camera_transform(Instance instance, int ptr) {
		if (CrateUtils.doesNotHavePermissionAndWarn(instance, GooseboyCrate.Permission.GPU)) return 0;
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return 0;

		GooseboyGpuCamera camera = gpu.camera;
		float[] values = {camera.getX(), camera.getY(), camera.getZ(), camera.getYaw(), camera.getPitch()};
		ByteBuffer transform = ByteBuffer.allocate(values.length * Float.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN);
		for (float value : values)
			transform.putFloat(value);
		instance.memory()
				.write(ptr, transform.array());
		return 1;
	}

	@WasmExport
	public int set_camera_transform(Instance instance, float x, float y, float z, float yaw, float pitch) {
		if (CrateUtils.doesNotHavePermissionAndWarn(instance, GooseboyCrate.Permission.GPU)) return 0;
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return 0;
		gpu.camera.setPosition(x, y, z);
		gpu.camera.rotation.set(yaw, pitch);
		return 1;
	}

	@WasmExport
	public int submit_gpu_commands(Instance instance, int ptr, int count) {
		if (CrateUtils.doesNotHavePermissionAndWarn(instance, GooseboyCrate.Permission.GPU)) return 0;
		Memory memory = instance.memory();
		GooseboyGpuRenderer gpu = GooseboyClient.rendererByInstance.get(instance);
		if (gpu == null) return 0;

		boolean canLog = CrateUtils.canLog(instance);
		int offset = ptr;
		int end = ptr + count;

		while (offset < end) {
			byte cmdId = memory.read(offset);
			GpuCommand cmd = GpuCommand.findCommandById(cmdId);

			int payloadLength = cmd.len();

			// TODO improve this
			if (cmd.id() == GpuCommand.RegisterTexture.id()) {
				// width * height * 4
				int width = memory.readInt(offset + 1);
				int height = memory.readInt(offset + 1 + 4);

				payloadLength += width * height * 4;
			}

			byte[] payload = memory.readBytes(offset + 1, payloadLength);

			gpu.queuedCommands.add(
					new QueuedCommand(cmd, payload, canLog)
			);

			offset += 1 + payloadLength;
		}

		return 1;
	}

	@WasmExport
	public int gpu_read(Instance instance, int offset, int ptr, int len) {
		if (CrateUtils.doesNotHavePermissionAndWarn(instance, GooseboyCrate.Permission.GPU)) return 0;
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
