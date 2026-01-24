package awildgoose.gooseboy;

import awildgoose.gooseboy.lib.Registrar;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.experimental.dircache.DirectoryCache;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.ChicoryException;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import org.apache.commons.lang3.tuple.Pair;

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

	public static List<Pair<Path, Path>> listWasmCrates() {
		// Pair<goosePath, cratePath>
		Map<String, Pair<Path, Path>> crates = new LinkedHashMap<>();

		Consumer<Path> addWasmFiles = path -> {
			try (Stream<Path> stream = Files.list(path.resolve("crates"))) {
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
							crates.put(filename, Pair.of(path, f.toAbsolutePath()
									.normalize()));
						});
			} catch (IOException ignored) {
			}
		};

		Path wasmPath = Gooseboy.getGooseboyDirectory();
		if (Files.exists(wasmPath)) {
			addWasmFiles.accept(wasmPath);
		}

		Path homePath = Gooseboy.getHomeGooseboyDirectory();
		if (homePath != null && Files.exists(homePath)) {
			addWasmFiles.accept(homePath);
		}

		try {
			URL resource = Gooseboy.class.getResource("/assets/" + Gooseboy.MOD_ID + "/gooseboy/");
			if (resource != null) {
				if (resource.getProtocol()
						.equals("jar")) {
					JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
					JarFile jarFile = jarConnection.getJarFile();
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();

						if (name.startsWith("assets/" + Gooseboy.MOD_ID + "/gooseboy/") && isValidGooseboyFilename(
								name)) {
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
							crates.put(filename, Pair.of(homePath, full));
						}
					}
				} else if (resource.getProtocol()
						.equals("file")) {
					Path dir = Paths.get(resource.toURI());
					addWasmFiles.accept(dir);
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
										  int maximumMemoryKilobytes, Path goosePath) throws ChicoryException {
		int initialPages = kilobytesToPages(initialMemoryKilobytes);
		int maxPages = kilobytesToPages(maximumMemoryKilobytes);

		WasmModule module = Parser.parse(wasm);
		Instance.Builder builder = Instance.builder(module)
				.withImportValues(Registrar.register(ImportValues.builder())
										  .build());

		if (!ConfigManager.getConfig().use_interpreter) {
			MachineFactoryCompiler.Builder machine = MachineFactoryCompiler.builder(module);
			if (ConfigManager.getConfig().experimental_use_compiler_cache)
				machine.withCache(new DirectoryCache(goosePath.resolve("cache")));
			builder.withMachineFactory(
					machine.compile());
		}

		builder.withMemoryLimits(
				new MemoryLimits(initialPages, maxPages)
		);

		// This takes quite a bit for big crates
		// I think that's reasonable, though
		return builder.build();
	}
}
