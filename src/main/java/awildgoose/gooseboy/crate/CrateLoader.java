package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.RawImage;
import awildgoose.gooseboy.Wasm;
import com.dylibso.chicory.wasm.ChicoryException;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CrateLoader {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.setStrictness(Strictness.LENIENT)
			.registerTypeAdapter(GooseboyCrate.Permission.class, (JsonDeserializer<GooseboyCrate.Permission>) (json, typeOfT, ctx) -> {
				try {
					return GooseboyCrate.Permission.valueOf(json.getAsString());
				} catch (Exception e) {
					Gooseboy.LOGGER.warn("Unknown permission: " + json.getAsString());
					return null;
				}
			})
			.create();

	private static Path resolvePath(String relPath) {
		return Gooseboy.getGooseboyDirectory().resolve("crates").resolve(relPath);
	}

	public static CrateMeta loadCrate(String relPath) throws IOException, JsonSyntaxException {
		var file = resolvePath(relPath).toFile();
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

	public static GooseboyCrate makeCrate(String filename) throws IOException, ChicoryException {
		CrateMeta meta = CrateLoader.loadCrate(filename);
		var permissions = ConfigManager.getEffectivePermissions(filename);
		if (!(new HashSet<>(permissions).containsAll(meta.permissions))) {
			throw new RuntimeException("Missing permissions: " + permissions.stream().filter(f -> !meta.permissions.contains(f)).map(
					Enum::name).toList());
		}

		var wasm = meta.binary;

		// TODO do we really need these to be separate values?
		var memoryLimits = ConfigManager.getMemoryLimits(filename);
		int initialMemory = memoryLimits.getLeft();
		int maxMemory = memoryLimits.getRight();
		var instance = Wasm.createInstance(wasm, initialMemory, maxMemory);

		return new GooseboyCrate(instance, filename, meta);
	}
}
