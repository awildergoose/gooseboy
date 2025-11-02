package awildgoose.gooseboy.lib;

import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;

public class Registrar {
	public static ImportValues.Builder register(ImportValues.Builder builder) {
		Console console = new Console();
		Framebuffer fb = new Framebuffer();
		Mem mem = new Mem();
		Input input = new Input();
		Audio audio = new Audio();
		Storage storage = new Storage();

		HostFunction[] consoleHostFunctions = console.toHostFunctions();
		HostFunction[] framebufferHostFunctions = fb.toHostFunctions();
		HostFunction[] memHostFunctions = mem.toHostFunctions();
		HostFunction[] inputHostFunctions = input.toHostFunctions();
		HostFunction[] audioHostFunctions = audio.toHostFunctions();
		HostFunction[] storageHostFunctions = storage.toHostFunctions();

		return builder.addFunction(consoleHostFunctions)
				.addFunction(framebufferHostFunctions)
				.addFunction(memHostFunctions)
				.addFunction(inputHostFunctions)
				.addFunction(audioHostFunctions)
				.addFunction(storageHostFunctions);
	}
}
