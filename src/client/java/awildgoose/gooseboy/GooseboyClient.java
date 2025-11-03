package awildgoose.gooseboy;

import awildgoose.gooseboy.crate.WasmCrate;
import awildgoose.gooseboy.screen.WasmMenuScreen;
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
		// TODO wasm module selection screen

		Gooseboy.ccb = new ClientCommonBridgeImpl();
		ClientTickEvents.END_WORLD_TICK.register(w -> {
			if (keyOpenWasm.isDown()) //noinspection CommentedOutCode
			{
				Minecraft mc = Minecraft.getInstance();

				/*var name = "test";
				var crate = new WasmCrate(Wasm.createInstance(name), name);*/
				mc.setScreen(new WasmMenuScreen(/*crate*/));
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(c -> RawAudioManager.tick());
	}
}