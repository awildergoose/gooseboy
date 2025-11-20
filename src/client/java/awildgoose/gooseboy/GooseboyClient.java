package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.CrateListScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class GooseboyClient implements ClientModInitializer {
	private static final KeyMapping.Category keyMappingCategory = KeyMapping.Category.register(
			ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "wasm"));
	public final KeyMapping keyOpenWasm = new KeyMapping(
			"key.open_wasm", InputConstants.KEY_M,
														keyMappingCategory);

	@Override
	public void onInitializeClient() {
		Gooseboy.ccb = new ClientCommonBridgeImpl();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			RawAudioManager.tick();

			if (client.player == null) return;

			if (keyOpenWasm.isDown())
				Minecraft.getInstance()
						.setScreen(new CrateListScreen());
		});
		// TODO is this really a good idea..
		WorldRenderEvents.END_MAIN.register(
				ResourceLocation.fromNamespaceAndPath(Gooseboy.MOD_ID, "input_updater"),
				(context) -> WasmInputManager.update());
		KeyBindingHelper.registerKeyBinding(keyOpenWasm);
	}
}