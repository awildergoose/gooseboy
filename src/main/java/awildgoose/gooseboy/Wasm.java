package awildgoose.gooseboy;

import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.ChicoryException;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.UninstantiableException;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
		Set<String> scripts = new HashSet<>();

		Consumer<Stream<Path>> addWasmFiles = stream ->
				stream.filter(Files::isRegularFile)
						.filter(f -> f.getFileName().toString().toLowerCase().endsWith(".wasm"))
						.map(f -> f.getFileName().toString())
						.forEach(scripts::add);

		Path wasmPath = Gooseboy.getGooseboyDirectory().resolve("scripts");
		if (Files.exists(wasmPath)) {
			try (Stream<Path> stream = Files.list(wasmPath)) {
				addWasmFiles.accept(stream);
			} catch (IOException ignored) {}
		}

		try {
			URL resource = Gooseboy.class.getResource("/assets/gooseboy/scripts/");

			if (resource != null) {
				if ("jar".equals(resource.getProtocol())) {
					JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
					JarFile jarFile = jarConnection.getJarFile();
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.startsWith("assets/gooseboy/scripts/") && name.endsWith(".wasm")) {
							scripts.add(Paths.get(name).getFileName().toString());
						}
					}
				} else if ("file".equals(resource.getProtocol())) {
					Path dir = Paths.get(resource.toURI());
					try (Stream<Path> stream = Files.list(dir)) {
						addWasmFiles.accept(stream);
					}
				}
			}
		} catch (Exception ignored) {}

		return new ArrayList<>(scripts);
	}

	public static final int WASM_PAGE_SIZE_KB = 64;

	private static int kilobytesToPages(int kilobytes) {
		return (kilobytes + WASM_PAGE_SIZE_KB - 1) / WASM_PAGE_SIZE_KB;
	}

	public static @Nullable Instance createInstance(String filename, int initialMemoryKilobytes, int maximumMemoryKilobytes) {
		var wasm = loadWasm(filename);
		if (wasm.isEmpty()) return null;

		int initialPages = kilobytesToPages(initialMemoryKilobytes);
		int maxPages = kilobytesToPages(maximumMemoryKilobytes);

		var module = Parser.parse(wasm.get());
		var builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder()).build());

		if (!ConfigManager.getConfig().useInterpreter)
			builder.withMachineFactory(
						MachineFactoryCompiler::compile).withMemoryLimits(
						new MemoryLimits(initialPages, maxPages)
				);

		Instance instance;

		try {
			instance = builder.build();
		} catch (ChicoryException e) {
			instance = null;
			e.printStackTrace();

			if (e instanceof UninstantiableException) {
				Gooseboy.LOGGER.error("You may need to set the EXTENDED_MEMORY permission for this crate to work");
			}
		}

		return instance;
	}

}
