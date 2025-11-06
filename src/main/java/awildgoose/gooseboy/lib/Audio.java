package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.crate.CrateUtils;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.Instance;

@HostModule("audio")
public final class Audio {
	@WasmExport
	public long play_audio(Instance instance, int ptr, int len) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return -1;
		byte[] pcm = instance.memory().readBytes(ptr, len);
		return Gooseboy.ccb.playRawAudio(pcm);
	}

	@WasmExport
	public void stop_audio(Instance instance, long id) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return;
		Gooseboy.ccb.stopAudio(id);
	}

	@WasmExport
	public void stop_all_audio(Instance instance) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return;
		Gooseboy.ccb.stopAllAudio();
	}

	@WasmExport
	public void set_audio_volume(Instance instance, long id, float volume) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return;
		Gooseboy.ccb.setAudioVolume(id, volume);
	}

	@WasmExport
	public void set_audio_pitch(Instance instance, long id, float pitch) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return;
		Gooseboy.ccb.setAudioPitch(id, pitch);
	}

	@WasmExport
	public int is_audio_playing(Instance instance, long id) {
		if (CrateUtils.doesNotHavePermission(instance, GooseboyCrate.Permission.AUDIO))
			return 0;
		return Gooseboy.ccb.isAudioPlaying(id) ? 1 : 0;
	}

	public HostFunction[] toHostFunctions() {
		return Audio_ModuleFactory.toHostFunctions(this);
	}
}
