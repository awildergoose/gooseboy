package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.WasmCrate;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;

public final class ConfigManager {
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("gooseboy.json");
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(WasmCrate.Permission.class, (JsonDeserializer<WasmCrate.Permission>) (json, typeOfT, ctx) -> {
				try {
					return WasmCrate.Permission.valueOf(json.getAsString());
				} catch (Exception e) {
					Gooseboy.LOGGER.warn("Unknown permission: " + json.getAsString());
					return null;
				}
			})
			.create();

	public static class CrateSetting {
		public List<WasmCrate.Permission> permissions = new ArrayList<>();
	}

	public static class RootConfig {
		@SuppressWarnings("unused") public String version = "1.0.0";
		public Map<String, CrateSetting> crate_settings = new HashMap<>();
		public CrateSetting default_crate_settings = new CrateSetting();
	}

	private static RootConfig config;

	public static synchronized RootConfig getConfig() {
		if (config == null) load();
		return config;
	}

	public static synchronized void load() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			if (Files.exists(CONFIG_PATH)) {
				try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
					config = GSON.fromJson(r, RootConfig.class);
					if (config == null) config = new RootConfig();
				}
			} else {
				config = new RootConfig();
				config.default_crate_settings.permissions = Arrays.asList(
						WasmCrate.Permission.CONSOLE, WasmCrate.Permission.INPUT_MOUSE, WasmCrate.Permission.INPUT_MOUSE_POS
				);
				save();
			}

			sanitizePermissions(config);
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to load config, using defaults", e);
			config = new RootConfig();
		}
	}

	private static void sanitizePermissions(RootConfig cfg) {
		if (cfg.default_crate_settings != null) {
			cfg.default_crate_settings.permissions.removeIf(Objects::isNull);
		} else {
			cfg.default_crate_settings = new CrateSetting();
		}

		if (cfg.crate_settings != null) {
			for (CrateSetting s : cfg.crate_settings.values()) {
				if (s != null && s.permissions != null) s.permissions.removeIf(Objects::isNull);
			}
		} else {
			cfg.crate_settings = new HashMap<>();
		}
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Path tmp = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName().toString() + ".tmp");

			try (Writer w = Files.newBufferedWriter(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				GSON.toJson(getConfig(), w);
			}

			try {
				Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			Gooseboy.LOGGER.error("Failed to save config", e);
		}
	}

	public static synchronized List<WasmCrate.Permission> getEffectivePermissions(String crateName) {
		RootConfig cfg = getConfig();
		CrateSetting specific = cfg.crate_settings.get(crateName);
		if (specific != null && specific.permissions != null && !specific.permissions.isEmpty()) {
			return List.copyOf(specific.permissions);
		}
		return List.copyOf(cfg.default_crate_settings.permissions);
	}

	public static synchronized void setCratePermissions(String crateName, Collection<WasmCrate.Permission> permissions) {
		RootConfig cfg = getConfig();
		CrateSetting s = new CrateSetting();
		s.permissions = new ArrayList<>(permissions);
		cfg.crate_settings.put(crateName, s);
		save();
	}

	public static synchronized void removeCrateConfig(String crateName) {
		RootConfig cfg = getConfig();
		cfg.crate_settings.remove(crateName);
		save();
	}
}
