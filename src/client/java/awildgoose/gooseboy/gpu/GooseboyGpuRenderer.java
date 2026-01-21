package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.GooseboyPainter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import org.joml.*;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

@Environment(EnvType.CLIENT)
public class GooseboyGpuRenderer implements AutoCloseable {
	private final VertexStack vertexStack;
	private final RenderSystem.AutoStorageIndexBuffer indices;
	private final TextureTarget renderTarget;

	public GooseboyGpuRenderer() {
		this.vertexStack = new VertexStack();
		this.indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		this.renderTarget = new TextureTarget(
				"gooseboy_goosegpu_framebuffer",
				FRAMEBUFFER_WIDTH,
				FRAMEBUFFER_HEIGHT,
				true
		);

		GooseboyPainter.pushCube(
				this.vertexStack,
				0f, 0f, 0f,
				16f, 16f, 16f);
	}

	public void render() {
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		float f = (float) (1.0 + Mth.sin(Util.getMillis()));
//		matrix4fStack.transform(f, f, f);

		GpuBuffer buffer = this.vertexStack.intoGpuBuffer();

		if (buffer != null) {
			GpuTextureView colorView = this.renderTarget.getColorTextureView();
			GpuTextureView depthView = this.renderTarget.getDepthTextureView();

			TextureManager textureManager = Minecraft.getInstance()
					.getTextureManager();
			AbstractTexture texture = textureManager.getTexture(WorldBorderRenderer.FORCEFIELD_LOCATION);

			float time = (float) (Util.getMillis() % 3000L) / 3000.0F;
			GpuBuffer indexBuffer = this.indices.getBuffer(6);
			GpuBufferSlice transformSlice = RenderSystem.getDynamicUniforms()
					.writeTransform(
							matrix4fStack,
							new Vector4f(1f, 1f, 1f, 1f),
							new Vector3f(f, f, f),
							new Matrix4f().translation(time, time, 0.0F),
							0.0F
					);

			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
							() -> "Gooseboy GooseGPU",
							colorView,
							OptionalInt.empty(),
							depthView,
							OptionalDouble.of(1.0)
					)) {
				renderPass.setPipeline(GooseboyClient.GOOSE_GPU_PIPELINE);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", transformSlice);
				renderPass.setIndexBuffer(indexBuffer, this.indices.type());
				renderPass.bindSampler("Sampler0", texture.getTextureView());
				renderPass.setVertexBuffer(0, buffer);

				renderPass.drawIndexed(0, 0, (vertexStack.size() / 4) * 6, 1);
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
				0.0f, 1.0f, 0.0f, 1.0f,
				0xFFFFFFFF,
				guiGraphics.scissorStack.peek()
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