package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Memory;

@HostModule("audio")
public final class Audio {
	@WasmExport
	public void play_audio(Memory memory, int ptr, int len) {
		byte[] pcm = memory.readBytes(ptr, len);
		Gooseboy.ccb.playRawAudio(pcm);
	}

	public HostFunction[] toHostFunctions() {
		return Audio_ModuleFactory.toHostFunctions(this);
	}
}
