package awildgoose.gooseboy.lib;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;

public class Registrar {
	public static ImportValues.Builder register(ImportValues.Builder builder) {
		Console console = new Console();
		Framebuffer fb = new Framebuffer();
		Mem mem = new Mem();
		HostFunction[] consoleHostFunctions = console.toHostFunctions();
		HostFunction[] framebufferHostFunctions = fb.toHostFunctions();
		HostFunction[] memHostFunctions = mem.toHostFunctions();

		return builder.addFunction(consoleHostFunctions).addFunction(framebufferHostFunctions).addFunction(memHostFunctions);
	}
}
