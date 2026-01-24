package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.CrateMeta;
import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.runtime.Instance;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class Gooseboy implements ModInitializer {
	public static final String MOD_ID = "gooseboy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ClientCommonBridge ccb;

	private static void makeDirectories(Path base) {
		try {
			Files.createDirectories(base);
			Files.createDirectories(base.resolve("crates"));
			Files.createDirectories(base.resolve("storage"));
			Files.createDirectories(base.resolve("cache"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Path getGooseboyDirectory() {
		Path gameDir = FabricLoader.getInstance()
				.getGameDir();
		Path gooseboyDir = gameDir.resolve("gooseboy");
		makeDirectories(gooseboyDir);
		return gooseboyDir;
	}

	public static @Nullable Path getHomeGooseboyDirectory() {
		String home = System.getenv("HOME");
		if (home == null || home.isEmpty()) {
			home = System.getenv("USERPROFILE");

			if (home == null || home.isEmpty()) {
				return null;
			}
		}

		Path folder = Paths.get(home)
				.resolve(".gooseboy");
		makeDirectories(folder);
		return folder;
	}

	public static Path getGooseboyCratesDirectory() {
		return Gooseboy.getGooseboyDirectory()
				.resolve("crates");
	}

	private static final ConcurrentHashMap<Instance, Pair<GooseboyCrate, CrateMeta>> runningCrates = new ConcurrentHashMap<>();

	public static GooseboyCrate getCrate(Instance instance) {
		return runningCrates.get(instance)
				.getLeft();
	}

	public static CrateMeta getCrateMeta(Instance instance) {
		return runningCrates.get(instance)
				.getRight();
	}

	public static void addCrate(GooseboyCrate crate, CrateMeta meta) {
		runningCrates.put(crate.instance, Pair.of(crate, meta));
	}

	public static void removeCrate(GooseboyCrate crate) {
		runningCrates.remove(crate.instance);
	}

	public static ConcurrentHashMap<Instance, Pair<GooseboyCrate, CrateMeta>> getCrates() {
		return runningCrates;
	}

	public static ResourceLocation withLocation(String path) {
		return ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// create dirs
		Gooseboy.getGooseboyDirectory();
	}
}