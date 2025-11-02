package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class GooseboyClient implements ClientModInitializer {
	public final KeyMapping keyOpenWasm = new KeyMapping("key.open_wasm", InputConstants.KEY_M,
														 KeyMapping.Category.MISC);

	@Override
	public void onInitializeClient() {
		Gooseboy.ccb = new ClientCommonBridgeImpl();
		ClientTickEvents.END_WORLD_TICK.register(w -> {
			if (keyOpenWasm.isDown()) {
				Minecraft mc = Minecraft.getInstance();
				mc.setScreen(new WasmScreen(Wasm.getInstance(), "test"));
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(c -> RawAudioManager.tick());
	}
}