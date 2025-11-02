package awildgoose.gooseboy;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gooseboy implements ModInitializer {
	public static final String MOD_ID = "gooseboy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ClientCommonBridge ccb;

	public static final int FRAMEBUFFER_WIDTH = 320;
	public static final int FRAMEBUFFER_HEIGHT = 200;

	@Override
	public void onInitialize() {

	}
}