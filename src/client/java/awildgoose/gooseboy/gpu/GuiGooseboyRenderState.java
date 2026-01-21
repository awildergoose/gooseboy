package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.GooseboyClient;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.profiling.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static net.minecraft.client.renderer.WorldBorderRenderer.FORCEFIELD_LOCATION;

@Environment(EnvType.CLIENT)
public final class GuiGooseboyRenderState implements GuiElementRenderState {
	private final RenderPipeline pipeline;
	private final TextureSetup textureSetup;
	private @Nullable ScreenRectangle scissorArea;
	private @Nullable ScreenRectangle bounds;
	private final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(
			VertexFormat.Mode.QUADS);
	public VertexStack stack;

	public GuiGooseboyRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup,
			@Nullable ScreenRectangle scissorArea,
			@Nullable ScreenRectangle bounds
	) {
		this.pipeline = pipeline;
		this.textureSetup = textureSetup;
		this.scissorArea = scissorArea;
		this.bounds = bounds;
		this.stack = new VertexStack();
	}

	public GuiGooseboyRenderState(
			RenderPipeline pipeline,
			TextureSetup textureSetup,
			@Nullable ScreenRectangle scissorArea,
			int x,
			int y
	) {
		this(
				pipeline,
				textureSetup,
				scissorArea,
				getBounds(x, y, scissorArea)
		);
	}

	@Nullable
	private static ScreenRectangle getBounds(int x, int y, @Nullable ScreenRectangle scissor) {
		ScreenRectangle rect = new ScreenRectangle(x, y, Gooseboy.FRAMEBUFFER_WIDTH, Gooseboy.FRAMEBUFFER_HEIGHT);
		return scissor != null ? scissor.intersection(rect) : rect;
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		GlStateManager._disableDepthTest();
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		matrix4fStack.translate(0, 0, -1000);
		matrix4fStack.translate(this.bounds.left(), 30, 0);

		GpuBuffer buffer = this.stack.intoGpuBuffer();

		if (buffer != null && this.bounds != null) {
			TextureManager textureManager = Minecraft.getInstance()
					.getTextureManager();
			AbstractTexture abstractTexture = textureManager.getTexture(FORCEFIELD_LOCATION);
			abstractTexture.setUseMipmaps(false);
			RenderTarget renderTarget = Minecraft.getInstance()
					.getMainRenderTarget();
			GpuTextureView gpuTextureView = renderTarget.getColorTextureView();
			GpuTextureView gpuTextureView2 = renderTarget.getDepthTextureView();

			float l = (float) (Util.getMillis() % 3000L) / 3000.0F;
			GpuBuffer gpuBuffer = this.indices.getBuffer(6);
			GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
					.writeTransform(
							RenderSystem.getModelViewMatrix(),
							new Vector4f(1f, 0f, 0f, 1f),
							new Vector3f(0f, 0f, 0f),
							new Matrix4f().translation(l, l, 0.0F),
							0.0F
					);

			Profiler.get()
					.push("GooseGPU");

			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "Gooseboy GooseGPU", gpuTextureView,
									  OptionalInt.empty(), gpuTextureView2,
									  OptionalDouble.empty())) {
				renderPass.setPipeline(GooseboyClient.GOOSE_GPU_PIPELINE);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
				renderPass.setIndexBuffer(gpuBuffer, this.indices.type());
				renderPass.bindSampler("Sampler0", abstractTexture.getTextureView());
				renderPass.setVertexBuffer(0, buffer);

				renderPass.drawIndexed(0, 0, (stack.size() / 4) * 6, 1);
			}

			Profiler.get()
					.pop();
		}

		matrix4fStack.popMatrix();
	}

	@Override
	public @NotNull RenderPipeline pipeline() {
		return pipeline;
	}

	@Override
	public @NotNull TextureSetup textureSetup() {
		return textureSetup;
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return scissorArea;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		return bounds;
	}

	public void setBounds(int x, int y, @Nullable ScreenRectangle peek) {
		this.scissorArea = peek;
		this.bounds = getBounds(x, y, scissorArea);
	}
}
