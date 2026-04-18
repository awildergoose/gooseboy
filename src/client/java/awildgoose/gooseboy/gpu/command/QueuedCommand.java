package awildgoose.gooseboy.gpu.command;

import awildgoose.gooseboy.gpu.consumer.GpuMemoryReader;

public record QueuedCommand(GpuCommand command, GpuMemoryReader reader) {
	public QueuedCommand(GpuCommand command, byte[] reader, boolean canLog) {
		this(command, new GpuMemoryReader(reader, canLog));
	}
}
