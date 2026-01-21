package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.GooseboyClient;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static net.minecraft.client.renderer.WorldBorderRenderer.FORCEFIELD_LOCATION;

@Environment(EnvType.CLIENT)
public final class GuiGooseboyRenderState implements GuiElementRenderState {
	private final RenderPipeline pipeline;
	private final TextureSetup textureSetup;
	private final PoseStack pose;
	private final @Nullable ScreenRectangle scissorArea;
	private final @Nullable ScreenRectangle bounds;
	private final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(
			VertexFormat.Mode.QUADS);
	public VertexStack stack;
	public GuiGooseboyRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup, PoseStack pose,
			@Nullable ScreenRectangle scissorArea,
			@Nullable ScreenRectangle bounds
	) {
		this.pipeline = pipeline;
		this.textureSetup = textureSetup;
		this.pose = pose;
		this.scissorArea = scissorArea;
		this.bounds = bounds;
	}

	public GuiGooseboyRenderState(
			RenderPipeline pipeline,
			TextureSetup textureSetup,
			PoseStack pose,
			@Nullable ScreenRectangle scissorArea,
			int x,
			int y
	) {
		this(
				pipeline,
				textureSetup,
				pose,
				scissorArea,
				getBounds(x, y, scissorArea)
		);
		this.stack = new VertexStack();
	}

	@Nullable
	private static ScreenRectangle getBounds(int x, int y, @Nullable ScreenRectangle scissor) {
		ScreenRectangle rect = new ScreenRectangle(x, y, Gooseboy.FRAMEBUFFER_WIDTH, Gooseboy.FRAMEBUFFER_HEIGHT);
		return scissor != null ? scissor.intersection(rect) : rect;
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		this.pose.pushPose();
		PoseStack.Pose pose = this.pose.last();
		GpuBuffer buffer = this.stack.intoGpuBuffer(pose);
		RenderTarget renderTarget = Minecraft.getInstance()
				.getMainRenderTarget();
		GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
				.writeTransform(
						// TODO
						RenderSystem.getModelViewMatrix(),
						new Vector4f(1f, 1f, 1f, 1f),
						new Vector3f(),
						new Matrix4f(),
						0.0F
				);
		TextureManager textureManager = Minecraft.getInstance()
				.getTextureManager();
		AbstractTexture abstractTexture = textureManager.getTexture(FORCEFIELD_LOCATION);
		abstractTexture.setUseMipmaps(false);

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "Gooseboy GooseGPU", renderTarget.getColorTextureView(),
								  OptionalInt.empty(), renderTarget.getDepthTextureView(), OptionalDouble.empty())) {
			renderPass.setPipeline(GooseboyClient.GOOSE_GPU_PIPELINE);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
			renderPass.setIndexBuffer(this.indices.getBuffer(6), this.indices.type());
			renderPass.bindSampler("Sampler0", abstractTexture.getTextureView());
			renderPass.setVertexBuffer(0, buffer);
			ArrayList<RenderPass.Draw<GuiGooseboyRenderState>> draws = new ArrayList<>();

			int quadCount = stack.size() / 4;
			for (int i = 0; i < quadCount; i++) {
				draws.add(new RenderPass.Draw<>(
						0,
						buffer,
						indices.getBuffer(6),
						indices.type(),
						i * 6,
						6
				));
			}

			renderPass.drawMultipleIndexed(draws, null, null, Collections.emptyList(), this);
		}

		this.pose.popPose();
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
}
