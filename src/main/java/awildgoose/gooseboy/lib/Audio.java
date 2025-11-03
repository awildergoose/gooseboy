package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.WasmCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("audio")
public final class Audio {
	@WasmExport
	public void play_audio(Instance instance, int ptr, int len) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.AUDIO))
			return;
		byte[] pcm = instance.memory().readBytes(ptr, len);
		Gooseboy.ccb.playRawAudio(pcm);
	}

	public HostFunction[] toHostFunctions() {
		return Audio_ModuleFactory.toHostFunctions(this);
	}
}
