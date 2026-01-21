package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.GooseboyPainter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@Environment(EnvType.CLIENT)
public class Gooseboy3DRenderer {
	private final VertexStack vertexStack;
	private final RenderSystem.AutoStorageIndexBuffer indices;
	private GpuBuffer vertexBuffer;

	public Gooseboy3DRenderer() {
		this.vertexStack = new VertexStack();
		this.indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GooseboyPainter.pushCube(this.vertexStack, 0f, 0f, 0f, 16f, 16f, 16f);
	}

	public void render(int screenX, int screenY) {
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();

		matrix4fStack.translate(screenX, screenY, 0);

		GpuBuffer buffer = this.vertexStack.intoGpuBuffer();

		if (buffer != null) {
			RenderTarget renderTarget = Minecraft.getInstance()
					.getMainRenderTarget();
			GpuTextureView colorView = renderTarget.getColorTextureView();
			GpuTextureView depthView = renderTarget.getDepthTextureView();

			TextureManager textureManager = Minecraft.getInstance()
					.getTextureManager();
			AbstractTexture texture = textureManager.getTexture(WorldBorderRenderer.FORCEFIELD_LOCATION);

			float time = (float) (Util.getMillis() % 3000L) / 3000.0F;
			GpuBuffer indexBuffer = this.indices.getBuffer(6);
			GpuBufferSlice transformSlice = RenderSystem.getDynamicUniforms()
					.writeTransform(
							RenderSystem.getModelViewMatrix(),
							new Vector4f(1f, 1f, 1f, 1f),
							new Vector3f(0f, 0f, 0f),
							new Matrix4f().translation(time, time, 0.0F),
							0.0F
					);

			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
							() -> "Gooseboy 3D",
							colorView,
							OptionalInt.empty(),
							depthView,
							OptionalDouble.empty()
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
}