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
	public long play_audio(Instance instance, int ptr, int len) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.AUDIO))
			return -1;
		byte[] pcm = instance.memory().readBytes(ptr, len);
		return Gooseboy.ccb.playRawAudio(pcm);
	}

	@WasmExport
	public void stop_audio(Instance instance, long id) {
		if (!Gooseboy.getCrate(instance).permissions.contains(WasmCrate.Permission.AUDIO))
			return;
		Gooseboy.ccb.stopAudio(id);
	}

	public HostFunction[] toHostFunctions() {
		return Audio_ModuleFactory.toHostFunctions(this);
	}
}
