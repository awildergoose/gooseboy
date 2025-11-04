package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class GooseboyClient implements ClientModInitializer {
	public final KeyMapping keyOpenWasm = new KeyMapping("key.open_wasm", InputConstants.KEY_M,
														 KeyMapping.Category.MISC);

	@Override
	public void onInitializeClient() {
		Gooseboy.ccb = new ClientCommonBridgeImpl();
		ClientTickEvents.END_WORLD_TICK.register(w -> {
			if (keyOpenWasm.isDown())
				Minecraft.getInstance()
					.setScreen(new WasmMenuScreen());
		});
		ClientTickEvents.END_CLIENT_TICK.register(c -> RawAudioManager.tick());
		WorldRenderEvents.END_MAIN.register(
				ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "input_updater"),
				(context) -> WasmInputManager.update());
	}
}