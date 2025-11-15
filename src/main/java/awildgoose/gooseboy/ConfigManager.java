package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Pair;

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

	public static class CrateSettings {
		public List<GooseboyCrate.Permission> permissions = new ArrayList<>();
		public int initial_memory = 6 * 1024; // 6 KB
		public int max_memory = 8 * 1024; // 8 KB
	}

	public static class RootConfig {
		@SuppressWarnings("unused") public String version = "1.1.0";
		public boolean use_interpreter = false;
		public Map<String, CrateSettings> crate_settings = new HashMap<>();
		public CrateSettings default_crate_settings = new CrateSettings();
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
						GooseboyCrate.Permission.CONSOLE, GooseboyCrate.Permission.INPUT_MOUSE, GooseboyCrate.Permission.INPUT_MOUSE_POS
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
			cfg.default_crate_settings = new CrateSettings();
		}

		if (cfg.crate_settings != null) {
			for (CrateSettings s : cfg.crate_settings.values()) {
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

	public static synchronized List<GooseboyCrate.Permission> getEffectivePermissions(String crateName) {
		RootConfig cfg = getConfig();
		CrateSettings specific = cfg.crate_settings.get(crateName);
		if (specific != null && specific.permissions != null && !specific.permissions.isEmpty()) {
			return List.copyOf(specific.permissions);
		}
		return List.copyOf(cfg.default_crate_settings.permissions);
	}

	public static synchronized Pair<Integer, Integer> getMemoryLimits(String crateName) {
		RootConfig cfg = getConfig();
		CrateSettings specific = cfg.crate_settings.get(crateName);
		if (specific != null) {
			return Pair.of(specific.initial_memory, specific.max_memory);
		}

		return Pair.of(cfg.default_crate_settings.initial_memory, cfg.default_crate_settings.max_memory);
	}

	private static CrateSettings getOrCreateSettings(String crateName) {
		RootConfig cfg = getConfig();
		return cfg.crate_settings.computeIfAbsent(crateName, k -> new CrateSettings());
	}

	public static synchronized void setCratePermissions(String crateName, Collection<GooseboyCrate.Permission> permissions) {
		CrateSettings s = getOrCreateSettings(crateName);
		s.permissions = new ArrayList<>(permissions);
		save();
	}

	public static synchronized void setCrateMemoryLimits(String crateName, Pair<Integer, Integer> memoryLimits) {
		CrateSettings s = getOrCreateSettings(crateName);
		s.initial_memory = memoryLimits.getLeft();
		s.max_memory = memoryLimits.getRight();
		save();
	}
}
