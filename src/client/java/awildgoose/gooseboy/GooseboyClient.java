package awildgoose.gooseboy;

import awildgoose.gooseboy.gpu.render.GooseboyGpuRenderer;
import awildgoose.gooseboy.screen.CrateListScreen;
import com.dylibso.chicory.runtime.Instance;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.concurrent.ConcurrentHashMap;

public class GooseboyClient implements ClientModInitializer {
	public static final RenderPipeline.Snippet GOOSE_GPU_SNIPPET = RenderPipeline.builder(
					RenderPipelines.MATRICES_PROJECTION_SNIPPET)
			.withVertexShader(Gooseboy.withLocation("core/rendertype_goose_gpu"))
			.withFragmentShader(Gooseboy.withLocation("core/rendertype_goose_gpu"))
			.withSampler("Sampler0")
			.buildSnippet();
	public static final RenderPipeline TRIANGLES_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(GOOSE_GPU_SNIPPET)
					.withLocation(Gooseboy.withLocation("pipeline/goose_gpu_triangles"))
					.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES)
					.build()
	);
	public static final RenderPipeline QUADS_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(GOOSE_GPU_SNIPPET)
					.withLocation(Gooseboy.withLocation("pipeline/goose_gpu_quads"))
					.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
					.build()
	);
	private static final KeyMapping.Category keyMappingCategory = KeyMapping.Category.register(
			Gooseboy.withLocation("wasm"));
	public final KeyMapping keyOpenWasm = new KeyMapping(
			"key.open_wasm", InputConstants.KEY_M,
			keyMappingCategory);
	public static final ConcurrentHashMap<Instance, GooseboyGpuRenderer> rendererByInstance = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Instance, MiniView> miniviewsByInstance = new ConcurrentHashMap<>();

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

		HudElementRegistry.addLast(Gooseboy.withLocation("miniview"), (context, tickCounter) -> Gooseboy.getCrates()
				.forEach((instance, cratePair) -> {
					if (cratePair.getLeft().isMiniView) {
						MiniView miniview;

						if (miniviewsByInstance.containsKey(instance)) {
							miniview = miniviewsByInstance.get(instance);
						} else {
							miniview = new MiniView(cratePair.getLeft());
							miniview.init();
							miniviewsByInstance.put(instance, miniview);
						}

						miniview.render(context);
					}
				}));
	}
}