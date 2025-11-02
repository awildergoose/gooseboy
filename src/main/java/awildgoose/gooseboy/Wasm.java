package awildgoose.gooseboy;

import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Wasm {
	public static Optional<byte[]> loadWasm(String relativePath) {
		Path wasmPath = Gooseboy.getGooseboyDirectory().resolve(relativePath);

		if (!Files.exists(wasmPath)) {
			// Try loading from the JAR
			try (InputStream in = Gooseboy.class.getResourceAsStream("/assets/gooseboy/" + relativePath)) {
				if (in == null) {
					return Optional.empty();
				}

				return Optional.ofNullable(in.readAllBytes());
			} catch (IOException e) {
				return Optional.empty();
			}
		}

		try {
			return Optional.of(Files.readAllBytes(wasmPath));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public static Instance getInstance() {
		// TODO this function can throw UnlinkableException
		var wasm = loadWasm("test.wasm");
		if (wasm.isEmpty()) return null;

		var module = Parser.parse(wasm.get());
		var builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder()).build()).withMachineFactory(
						MachineFactoryCompiler::compile);

		var instance = builder.build();
		instance.export("main").apply();
		return instance;
	}

}
