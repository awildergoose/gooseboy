package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.RawImage;
import awildgoose.gooseboy.Wasm;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.ChicoryException;
import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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

	public static byte[] readAllBytesPossiblyFromJar(Path path) throws IOException {
		String raw = path.toString();
		String norm = raw.replace('\\', '/');

		// last .jar (for paths like G:/some.jar/assets/some.png)
		int jarIdx = norm.toLowerCase(Locale.ROOT)
				.lastIndexOf(".jar");

		if (jarIdx != -1) {
			String jarPathStr = norm.substring(0, jarIdx + 4);
			String entryPath = "";
			if (jarIdx + 4 < norm.length()) {
				int next = jarIdx + 4;
				if (norm.charAt(next) == '/') next++;
				entryPath = norm.substring(next);
			}

			Path jarPath = Paths.get(jarPathStr);

			if (Files.exists(jarPath) && !entryPath.isEmpty()) {
				String zipEntryName = entryPath.replace('\\', '/');

				try (ZipFile zip = new ZipFile(jarPath.toFile())) {
					ZipEntry entry = zip.getEntry(zipEntryName);
					if (entry == null) entry = zip.getEntry("/" + zipEntryName);
					if (entry == null) throw new FileNotFoundException(
							"Entry not found in jar: %s".formatted(zipEntryName));

					try (InputStream in = zip.getInputStream(entry)) {
						return in.readAllBytes();
					}
				}
			}
		}

		Path p = Paths.get(raw);
		if (Files.exists(p)) {
			return Files.readAllBytes(p);
		}

		throw new FileNotFoundException("File not found: %s".formatted(raw));
	}

	public static CrateMeta loadCrate(Path path) throws IOException, JsonSyntaxException {
		byte[] crateBytes = readAllBytesPossiblyFromJar(path);

		Map<String, byte[]> entries = new HashMap<>();
		try (ByteArrayInputStream bais = new ByteArrayInputStream(crateBytes);
			 ZipInputStream zis = new ZipInputStream(bais)) {
			ZipEntry ze;
			byte[] buf = new byte[8192];
			while ((ze = zis.getNextEntry()) != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int r;
				while ((r = zis.read(buf)) != -1) {
					baos.write(buf, 0, r);
				}
				entries.put(ze.getName(), baos.toByteArray());
				zis.closeEntry();
			}
		}

		Function<@NotNull String, byte[]> find = (name) -> {
			byte[] data = entries.get(name);
			if (data == null) throw new RuntimeException("Missing file in crate: %s".formatted(name));
			return data;
		};

		byte[] metaBytes = entries.get("crate.json");
		if (metaBytes == null) throw new RuntimeException("Crate meta missing");

		CrateMeta meta = GSON.fromJson(new String(metaBytes, StandardCharsets.UTF_8), CrateMeta.class);
		if (meta == null) throw new RuntimeException("Failed to parse crate meta");

		if (meta.icon != null) {
			byte[] iconBytes = find.apply(meta.icon);
			try (ByteArrayInputStream in = new ByteArrayInputStream(iconBytes)) {
				meta.iconImage = RawImage.load(in);
			}
		}

		if (meta.banner != null) {
			byte[] bannerBytes = find.apply(meta.banner);
			try (ByteArrayInputStream in = new ByteArrayInputStream(bannerBytes)) {
				meta.bannerImage = RawImage.load(in);
			}
		}

		meta.binary = find.apply(meta.entrypoint);
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
