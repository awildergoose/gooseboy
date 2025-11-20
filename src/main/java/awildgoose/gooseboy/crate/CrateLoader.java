package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.RawImage;
import awildgoose.gooseboy.Wasm;
import com.dylibso.chicory.wasm.ChicoryException;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CrateLoader {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.setStrictness(Strictness.LENIENT)
			.registerTypeAdapter(
					GooseboyCrate.Permission.class,
					(JsonDeserializer<GooseboyCrate.Permission>) (json, typeOfT, ctx) -> {
						try {
							return GooseboyCrate.Permission.valueOf(json.getAsString());
						} catch (Exception e) {
							Gooseboy.LOGGER.warn("Unknown permission: " + json.getAsString());
							return null;
						}
					})
			.create();

	public static @Nullable Path getHomeGooseboyCratesFolder() {
		String home = System.getenv("HOME");
		if (home == null || home.isEmpty()) {
			home = System.getenv("USERPROFILE");

			if (home == null || home.isEmpty()) {
				return null;
			}
		}

		Path folder = Paths.get(home)
				.resolve(".gooseboy");

		if (!Files.exists(folder)) {
			try {
				Files.createDirectories(folder);
			} catch (IOException ignored) {
			}
		}

		return folder;
	}

	public static Path getGooseboyCratesPath() {
		return Gooseboy.getGooseboyDirectory()
				.resolve("crates");
	}

	private static @Nullable Path resolvePath(String relPath) {
		var first = getGooseboyCratesPath()
				.resolve(relPath);
		if (Files.exists(first)) {
			return first;
		}

		var home = getHomeGooseboyCratesFolder();
		if (home != null)
			return home.resolve(relPath);
		return null;
	}

	public static CrateMeta loadCrate(String relPath) throws IOException, JsonSyntaxException {
		var path = resolvePath(relPath);
		if (path == null) {
			throw new RuntimeException("Failed to find crate: " + relPath);
		}
		var file = path.toFile();
		CrateMeta meta;

		try (ZipFile zipFile = new ZipFile(file)) {
			ZipEntry crateEntry = zipFile.getEntry("crate.json");
			if (crateEntry == null)
				throw new RuntimeException("Meta missing");

			try (InputStream in = zipFile.getInputStream(crateEntry)) {
				meta = GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), CrateMeta.class);
			}

			if (meta == null)
				throw new RuntimeException("Meta failed");

			Function<String, ZipEntry> find = name -> {
				ZipEntry e = zipFile.getEntry(name);
				if (e == null)
					throw new RuntimeException("Missing file in crate: " + name);
				return e;
			};

			if (meta.icon != null) {
				try (InputStream in = zipFile.getInputStream(find.apply(meta.icon))) {
					meta.iconImage = RawImage.load((ByteArrayInputStream) in);
				}
			}

			if (meta.banner != null) {
				try (InputStream in = zipFile.getInputStream(find.apply(meta.banner))) {
					meta.bannerImage = RawImage.load((ByteArrayInputStream) in);
				}
			}

			try (InputStream in = zipFile.getInputStream(find.apply(meta.entrypoint))) {
				meta.binary = in.readAllBytes();
			}
		}

		return meta;
	}

	public static GooseboyCrate makeCrate(String filename) throws IOException, ChicoryException, CrateLoaderException {
		CrateMeta meta = CrateLoader.loadCrate(filename);
		var permissions = ConfigManager.getEffectivePermissions(filename)
				.stream()
				.filter(Objects::nonNull)
				.toList();
		List<GooseboyCrate.Permission> missing = meta.permissions.stream()
				.filter(p -> !permissions.contains(p))
				.filter(Objects::nonNull)
				.toList();

		if (!missing.isEmpty()) {
			throw new CrateLoaderException("Missing permissions", missing.toString());
		}

		var wasm = meta.binary;

		// TODO do we really need these to be separate values?
		var memoryLimits = ConfigManager.getMemoryLimits(filename);
		int initialMemory = memoryLimits.getLeft();
		int maxMemory = memoryLimits.getRight();
		var instance = Wasm.createInstance(wasm, initialMemory, maxMemory);

		return new GooseboyCrate(instance, filename, meta);
	}

	public static class CrateLoaderException extends Exception {
		public String title, body;

		public CrateLoaderException(String title, String body) {
			super();
			this.title = title;
			this.body = body;
		}

		@Override
		public String getMessage() {
			return "%s: %s".formatted(this.title, this.body);
		}
	}
}
