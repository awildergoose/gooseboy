package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.RawImage;
import awildgoose.gooseboy.Wasm;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.ChicoryException;
import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

	public static CrateMeta loadCrate(Path path) throws IOException, JsonSyntaxException {
		File file = path.toFile();
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

	public static GooseboyCrate makeCrate(Path path) throws IOException, ChicoryException, CrateLoaderException {
		// TODO is this really a good way of doing it?
		Path goosePath = path.getParent()
				.getParent();
		String filename = path.getFileName()
				.toString();
		CrateMeta meta = CrateLoader.loadCrate(path);
		List<GooseboyCrate.Permission> permissions = ConfigManager.getEffectivePermissions(filename)
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

		byte[] wasm = meta.binary;

		// TODO do we really need these to be separate values?
		Pair<Integer, Integer> memoryLimits = ConfigManager.getMemoryLimits(filename);
		int initialMemory = memoryLimits.getLeft();
		int maxMemory = memoryLimits.getRight();
		Instance instance = Wasm.createInstance(wasm, initialMemory, maxMemory, goosePath);

		return new GooseboyCrate(instance, filename, meta, goosePath);
	}

	public static class CrateLoaderException extends Exception {
		public final String title;
		public final String body;

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
