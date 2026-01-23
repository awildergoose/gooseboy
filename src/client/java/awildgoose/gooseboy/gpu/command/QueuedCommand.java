package awildgoose.gooseboy.gpu.command;

import awildgoose.gooseboy.gpu.consumer.GpuMemoryReader;

public final class QueuedCommand {
	private final GpuCommand command;
	private final GpuMemoryReader reader;

	public QueuedCommand(GpuCommand command, byte[] payload) {
		this.command = command;
		this.reader = new GpuMemoryReader(payload);
	}

	public GpuCommand command() {
		return command;
	}

	public GpuMemoryReader reader() {
		return reader;
	}
}
