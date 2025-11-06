package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.GooseboyCrate;
import com.dylibso.chicory.runtime.Instance;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Gooseboy implements ModInitializer {
	public static final String MOD_ID = "gooseboy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ClientCommonBridge ccb;

	public static final int FRAMEBUFFER_WIDTH = 320;
	public static final int FRAMEBUFFER_HEIGHT = 200;

	public static Path getGooseboyDirectory() {
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path gooseboyDir = gameDir.resolve("gooseboy");

		try {
			Files.createDirectories(gooseboyDir);
			Files.createDirectories(gooseboyDir.resolve("crates"));
			Files.createDirectories(gooseboyDir.resolve("storage"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return gooseboyDir;
	}

	private static final HashMap<Instance, GooseboyCrate> runningCrates = new HashMap<>();

	public static GooseboyCrate getCrate(Instance instance) {
		return runningCrates.get(instance);
	}

	public static void addCrate(GooseboyCrate crate) {
		runningCrates.put(crate.instance, crate);
	}
	public static void removeCrate(GooseboyCrate crate) {
		runningCrates.remove(crate.instance);
	}

	@Override
	public void onInitialize() {
		// create dirs
		Gooseboy.getGooseboyDirectory();
	}
}