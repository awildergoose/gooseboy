package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.CrateLoader;
import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.ChicoryException;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;

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
	public static final int WASM_PAGE_SIZE_KB = 64;

	public static boolean isValidGooseboyFilename(String f) {
		return f.toLowerCase()
				.endsWith(".zip") || f.toLowerCase()
				.endsWith(".gbcrate");
	}

	public static List<Path> listWasmCrates() {
		Map<String, Path> crates = new LinkedHashMap<>();

		Consumer<Stream<Path>> addWasmFiles = stream ->
				stream.filter(Files::isRegularFile)
						.filter(f -> isValidGooseboyFilename(f.getFileName()
																	 .toString()))
						.forEach(f -> {
							String filename = f.getFileName()
									.toString();
							if (crates.containsKey(filename)) {
								Gooseboy.ccb.doTranslatedErrorMessage(
										"ui.gooseboy.duplicate_crate.title",
										"ui.gooseboy.duplicate_crate.body", filename);
								return;
							}
							crates.put(filename, f.toAbsolutePath()
									.normalize());
						});

		Path wasmPath = Gooseboy.getGooseboyDirectory()
				.resolve("crates");
		if (Files.exists(wasmPath)) {
			try (Stream<Path> stream = Files.list(wasmPath)) {
				addWasmFiles.accept(stream);
			} catch (IOException ignored) {
			}
		}

		Path homePath = CrateLoader.getHomeGooseboyCratesFolder();
		if (homePath != null && Files.exists(homePath)) {
			try (Stream<Path> stream = Files.list(homePath)) {
				addWasmFiles.accept(stream);
			} catch (IOException ignored) {
			}
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
						if (name.startsWith("assets/gooseboy/crates/")
								&& isValidGooseboyFilename(name)) {

							String filename = Paths.get(name)
									.getFileName()
									.toString();
							if (crates.containsKey(filename)) {
								Gooseboy.ccb.doTranslatedErrorMessage(
										"ui.gooseboy.duplicate_crate.title",
										"ui.gooseboy.duplicate_crate.body", filename);
								continue;
							}

							Path jarFilePath = Paths.get(jarFile.getName())
									.toAbsolutePath()
									.normalize();
							Path full = jarFilePath.resolve(name);
							crates.put(filename, full);
						}
					}
				} else if ("file".equals(resource.getProtocol())) {
					Path dir = Paths.get(resource.toURI());
					try (Stream<Path> stream = Files.list(dir)) {
						addWasmFiles.accept(stream);
					}
				}
			}
		} catch (Exception ignored) {
		}

		return new ArrayList<>(crates.values());
	}

	private static int kilobytesToPages(int kilobytes) {
		return (kilobytes + WASM_PAGE_SIZE_KB - 1) / WASM_PAGE_SIZE_KB;
	}

	public static Instance createInstance(byte[] wasm, int initialMemoryKilobytes,
										  int maximumMemoryKilobytes) throws ChicoryException {
		int initialPages = kilobytesToPages(initialMemoryKilobytes);
		int maxPages = kilobytesToPages(maximumMemoryKilobytes);

		WasmModule module = Parser.parse(wasm);
		Instance.Builder builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder())
										  .build());

		if (!ConfigManager.getConfig().use_interpreter)
			builder.withMachineFactory(
					MachineFactoryCompiler::compile);

		builder.withMemoryLimits(
				new MemoryLimits(initialPages, maxPages)
		);

		// This takes quite a bit for big crates
		// I think that's reasonable, though
		return builder.build();
	}
}
