package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.GooseboyPainter;
import awildgoose.gooseboy.WasmInputManager;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fStack;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

@Environment(EnvType.CLIENT)
public class GooseboyGpuRenderer implements AutoCloseable {
	private final VertexStack vertexStack;
	private final RenderSystem.AutoStorageIndexBuffer indices;
	private final TextureTarget renderTarget;
	private final GooseboyGpuCamera camera;

	public GooseboyGpuRenderer() {
		this.vertexStack = new VertexStack();
		this.indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		this.renderTarget = new TextureTarget(
				"gooseboy_goosegpu_framebuffer",
				FRAMEBUFFER_WIDTH,
				FRAMEBUFFER_HEIGHT,
				true
		);
		this.camera = new GooseboyGpuCamera();
		this.camera.setCameraPos(-200.0f, -200.0f, 0.0f);

		GooseboyPainter.pushCube(
				this.vertexStack,
				0f, 0f, 0f,
				16f, 16f, 16f);
	}

	public void render() {
		WasmInputManager.grabMouse();

		double f = 0.02;
		camera.setYaw((float) (camera.getYaw() - (WasmInputManager.LAST_ACCUMULATED_MOUSE_X * f)));
		camera.setPitch((float) (camera.getPitch() - (WasmInputManager.LAST_ACCUMULATED_MOUSE_Y * f)));

		float speed = 0.5f;

		if (WasmInputManager.isKeyDown(InputConstants.KEY_W)) {
			camera.moveForward(speed);
		}
		if (WasmInputManager.isKeyDown(InputConstants.KEY_S)) {
			camera.moveForward(-speed);
		}
		if (WasmInputManager.isKeyDown(InputConstants.KEY_A)) {
			camera.moveRight(-speed);
		}
		if (WasmInputManager.isKeyDown(InputConstants.KEY_D)) {
			camera.moveRight(speed);
		}

		if (WasmInputManager.isKeyDown(InputConstants.KEY_SPACE)) {
			camera.moveUp(speed);
		}
		if (WasmInputManager.isKeyDown(InputConstants.KEY_LSHIFT)) {
			camera.moveUp(-speed);
		}

		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();

		GpuBuffer buffer = this.vertexStack.intoGpuBuffer();

		if (buffer != null) {
			GpuTextureView colorView = this.renderTarget.getColorTextureView();
			GpuTextureView depthView = this.renderTarget.getDepthTextureView();

			TextureManager textureManager = Minecraft.getInstance()
					.getTextureManager();
			AbstractTexture texture = textureManager.getTexture(WorldBorderRenderer.FORCEFIELD_LOCATION);

			int quadCount = vertexStack.size() / 4;
			int indexCount = quadCount * 6;
			GpuBuffer indexBuffer = this.indices.getBuffer(indexCount);
			GpuBufferSlice transformSlice = this.camera.createTransformSlice();

			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
							() -> "Gooseboy GooseGPU",
							colorView,
							OptionalInt.of(0),
							depthView,
							OptionalDouble.of(1.0)
					)) {
				renderPass.setPipeline(GooseboyClient.GOOSE_GPU_PIPELINE);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", transformSlice);
				renderPass.bindSampler("Sampler0", texture.getTextureView());
				renderPass.setIndexBuffer(indexBuffer, this.indices.type());
				renderPass.setVertexBuffer(0, buffer);

				renderPass.drawIndexed(0, 0, indexCount, 1);
			}
		}

		matrix4fStack.popMatrix();
	}

	public void blitToScreen(GuiGraphics guiGraphics, int x, int y, int width, int height) {
		GpuTextureView textureView = this.renderTarget.getColorTextureView();

		BlitRenderState blitState = new BlitRenderState(
				RenderPipelines.GUI_TEXTURED,
				TextureSetup.singleTexture(textureView),
				new Matrix3x2f(),
				x, y, x + width, y + height,
				0.0f, 1.0f,
				1.0f, 0.0f,
				0xFFFFFFFF,
				null
		);

		guiGraphics.guiRenderState.submitGuiElement(blitState);
	}

	@Override
	public void close() {
		if (this.renderTarget != null) {
			this.renderTarget.destroyBuffers();
		}
	}
}