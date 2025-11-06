package awildgoose.gooseboy.lib;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.annotations.HostModule;
import com.dylibso.chicory.annotations.WasmExport;
import com.dylibso.chicory.runtime.HostFunction;

@HostModule("game")
public final class Game {
	public Game() {}

	@WasmExport
	public int get_minecraft_width() {
		return Gooseboy.ccb.getWindowWidth();
	}

	@WasmExport
	public int get_minecraft_height() {
		return Gooseboy.ccb.getWindowHeight();
	}

	@WasmExport
	public long get_time_nanos() {
		return System.nanoTime();
	}

	public HostFunction[] toHostFunctions() {
		return Game_ModuleFactory.toHostFunctions(this);
	}
}
