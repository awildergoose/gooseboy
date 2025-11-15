package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.ConfigManager;
import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.Wasm;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
		CrateMeta meta = null;

		try (ZipFile zipFile = new ZipFile(file)) {
			ZipEntry crateEntry = zipFile.getEntry("crate.json");
			if (crateEntry != null) {
				try (InputStream in = zipFile.getInputStream(crateEntry)) {
					String crateMeta = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					meta = GSON.fromJson(crateMeta, CrateMeta.class);
				}
			}

			if (meta == null)
				throw new RuntimeException("Meta failed");

			if (meta.icon != null) {
				CrateMeta finalMeta = meta;
				meta.rawIcon =
						zipFile.getInputStream(zipFile.stream().filter(e -> e.getName().equals(finalMeta.icon)).findFirst().orElseThrow()).readAllBytes();
			}

			if (meta.banner != null) {
				CrateMeta finalMeta = meta;
				meta.rawBanner =
						zipFile.getInputStream(zipFile.stream().filter(e -> e.getName().equals(finalMeta.banner)).findFirst().orElseThrow()).readAllBytes();
			}

			CrateMeta finalMeta = meta;
			meta.binary = zipFile.getInputStream(zipFile.stream().filter(e -> e.getName().equals(finalMeta.entrypoint)).findFirst().orElseThrow()).readAllBytes();
		}

		return meta;
	}

	public static GooseboyCrate makeCrate(String filename) {
		CrateMeta meta;

		try {
			meta = CrateLoader.loadCrate(filename);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		var wasm = meta.binary;
		var permissions = ConfigManager.getEffectivePermissions(filename);

		int initialMemory;
		int maxMemory;

		// TODO a better system instead of this mess
		if (permissions.contains(GooseboyCrate.Permission.EXTENDED_EXTENDED_MEMORY)) {
			initialMemory = 256 * 1024;
			maxMemory = 512 * 1024;
		} else if (permissions.contains(GooseboyCrate.Permission.EXTENDED_MEMORY)) {
			initialMemory = 32 * 1024;
			maxMemory = 64 * 1024;
		} else {
			initialMemory = 6 * 1024;
			maxMemory = 8 * 1024;
		}

		var instance = Wasm.createInstance(wasm, initialMemory, maxMemory);
		if (instance == null) {
			return null;
		}

		GooseboyCrate crate;

		try {
			crate = new GooseboyCrate(instance, filename, meta);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}

		return crate;
	}
}
