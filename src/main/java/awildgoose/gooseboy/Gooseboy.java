package awildgoose.gooseboy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gooseboy implements ModInitializer {
	public static final String MOD_ID = "gooseboy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ClientCommonBridge ccb;

	public static final int FRAMEBUFFER_WIDTH = 320;
	public static final int FRAMEBUFFER_HEIGHT = 200;

	public static Path getGooseboyDirectory() {
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path gooseboyDir = gameDir.resolve("gooseboy");

		if (!Files.exists(gooseboyDir)) {
			try {
				Files.createDirectories(gooseboyDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return gooseboyDir;
	}

	@Override
	public void onInitialize() {

	}
}