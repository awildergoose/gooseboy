package awildgoose.gooseboy;

import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.*;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
	public static boolean isValidGooseboyFilename(String f) {
		return f.toLowerCase().endsWith(".zip") || f.toLowerCase().endsWith(".gbcrate");
	}

	public static List<String> listWasmCrates() {
		Set<String> crates = new HashSet<>();

		Consumer<Stream<Path>> addWasmFiles = stream ->
				stream.filter(Files::isRegularFile)
						.filter(f -> isValidGooseboyFilename(f.getFileName().toString()))
						.map(f -> f.getFileName().toString())
						.forEach(crates::add);

		Path wasmPath = Gooseboy.getGooseboyDirectory().resolve("crates");
		if (Files.exists(wasmPath)) {
			try (Stream<Path> stream = Files.list(wasmPath)) {
				addWasmFiles.accept(stream);
			} catch (IOException ignored) {}
		}

		try {
			URL resource = Gooseboy.class.getResource("/assets/gooseboy/crates/");

			if (resource != null) {
				if ("jar".equals(resource.getProtocol())) {
					JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
					JarFile jarFile = jarConnection.getJarFile();
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.startsWith("assets/gooseboy/crates/") && isValidGooseboyFilename(name)) {
							crates.add(Paths.get(name).getFileName().toString());
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

		return new ArrayList<>(crates);
	}

	public static final int WASM_PAGE_SIZE_KB = 64;

	private static int kilobytesToPages(int kilobytes) {
		return (kilobytes + WASM_PAGE_SIZE_KB - 1) / WASM_PAGE_SIZE_KB;
	}

	public static @Nullable Instance createInstance(byte[] wasm, int initialMemoryKilobytes,
													int maximumMemoryKilobytes) {
		int initialPages = kilobytesToPages(initialMemoryKilobytes);
		int maxPages = kilobytesToPages(maximumMemoryKilobytes);

		WasmModule module;

		try {
			module = Parser.parse(wasm);
		} catch (MalformedException e) {
			e.printStackTrace();
			return null;
		}

		var builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder()).build());

		if (!ConfigManager.getConfig().useInterpreter)
			builder.withMachineFactory(
						MachineFactoryCompiler::compile);

		Instance instance;

		try {
			// This takes quite a bit for big crates
			// I think that's reasonable, though
			instance = builder.withMemoryLimits(
					new MemoryLimits(initialPages, maxPages)
			).build();
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
