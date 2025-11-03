package awildgoose.gooseboy;

import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.InvalidException;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.UnlinkableException;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Wasm {
	public static Optional<byte[]> loadWasm(String relativePath) {
		Path wasmPath = Gooseboy.getGooseboyDirectory().resolve("scripts").resolve(relativePath);

		if (!Files.exists(wasmPath)) {
			// Try loading from the JAR
			try (InputStream in = Gooseboy.class.getResourceAsStream(
					"/assets/gooseboy/scripts/" + relativePath
			)) {
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

	public static List<String> listWasmScripts() {
		Path wasmPath = Gooseboy.getGooseboyDirectory().resolve("scripts");

		try (Stream<Path> stream = Files.list(wasmPath)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(f -> f.getFileName().toString().toLowerCase().endsWith(".wasm"))
					.map(f -> f.getFileName().toString())
					.collect(Collectors.toList());
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	public static final int WASM_PAGE_SIZE_KB = 64;

	private static int kilobytesToPages(int kilobytes) {
		return (kilobytes + WASM_PAGE_SIZE_KB - 1) / WASM_PAGE_SIZE_KB;
	}

	public static @Nullable Instance createInstance(String filename, int maximumMemoryKilobytes) {
		var wasm = loadWasm(filename);
		if (wasm.isEmpty()) return null;

		var module = Parser.parse(wasm.get());
		var builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder()).build()).withMachineFactory(
						MachineFactoryCompiler::compile).withMemoryLimits(
						new MemoryLimits(0, kilobytesToPages(maximumMemoryKilobytes))
				);

		Instance instance = null;

		try {
			instance = builder.build();
			try {
				instance.export("main").apply();
			} catch (InvalidException e) {
				e.printStackTrace();
			}
		} catch (UnlinkableException e) {
			e.printStackTrace();
		}

		return instance;
	}

}
