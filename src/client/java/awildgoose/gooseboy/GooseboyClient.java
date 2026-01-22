package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.CrateListScreen;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

public class GooseboyClient implements ClientModInitializer {
	public static final RenderPipeline GOOSE_GPU_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
					.withLocation(Gooseboy.withLocation("pipeline/goose_gpu"))
					.withVertexShader(Gooseboy.withLocation("core/rendertype_goose_gpu"))
					.withFragmentShader(Gooseboy.withLocation("core/rendertype_goose_gpu"))
					.withSampler("Sampler0")
					// remove these \/
					.withBlend(BlendFunction.OVERLAY)
					.withCull(false)
					.withDepthWrite(false)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					// remove these /\
					.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
					.build()
	);
	private static final KeyMapping.Category keyMappingCategory = KeyMapping.Category.register(
			Gooseboy.withLocation("wasm"));
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
				Gooseboy.withLocation("input_updater"),
				(context) -> WasmInputManager.update());
		KeyBindingHelper.registerKeyBinding(keyOpenWasm);
	}
}