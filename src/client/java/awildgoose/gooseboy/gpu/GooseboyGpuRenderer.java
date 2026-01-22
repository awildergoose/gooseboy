package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.GooseboyClient;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.TextureTarget;
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
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_HEIGHT;
import static awildgoose.gooseboy.Gooseboy.FRAMEBUFFER_WIDTH;

@Environment(EnvType.CLIENT)
public class GooseboyGpuRenderer implements AutoCloseable {
	public final GooseboyGpuCamera camera = new GooseboyGpuCamera();

	private final RenderSystem.AutoStorageIndexBuffer indices;
	private final TextureTarget renderTarget;
	private final CachedPerspectiveProjectionMatrixBuffer projectionMatrixBuffer = new CachedPerspectiveProjectionMatrixBuffer(
			"gooseboy_goosegpu", camera.near, camera.far);

	public ArrayList<GooseboyGpu.QueuedCommand> queuedCommands = new ArrayList<>();
	private final MeshRegistry meshRegistry = new MeshRegistry();
	private final TextureRegistry textureRegistry = new TextureRegistry();
	public VertexStack globalVertexStack = new VertexStack();
	public AbstractTexture boundTexture = null;
	public final ByteBuffer gpuMemory = ByteBuffer.allocateDirect(4192 * 4)
			.order(ByteOrder.LITTLE_ENDIAN);

	public GooseboyGpuRenderer() {
		this.indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES);
		this.renderTarget = new TextureTarget(
				"gooseboy_goosegpu_framebuffer",
				FRAMEBUFFER_WIDTH,
				FRAMEBUFFER_HEIGHT,
				true
		);
	}

	public void renderVertexStack(VertexStack vertexStack) {
		GpuBuffer buffer = vertexStack.intoGpuBuffer();

		if (buffer != null) {
			RenderSystem.backupProjectionMatrix();
			RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(
					this.renderTarget.width,
					this.renderTarget.height,
					this.camera.fovDegrees
			), ProjectionType.PERSPECTIVE);

			GpuTextureView colorView = this.renderTarget.getColorTextureView();
			GpuTextureView depthView = this.renderTarget.getDepthTextureView();

			AbstractTexture texture = boundTexture;

			if (boundTexture == null) {
				TextureManager textureManager = Minecraft.getInstance()
						.getTextureManager();
				texture = textureManager.getTexture(WorldBorderRenderer.FORCEFIELD_LOCATION);
				boundTexture = texture;
			}

			int triCount = vertexStack.size() / 3;
			int indexCount = triCount * 3;
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

			RenderSystem.restoreProjectionMatrix();
		}
	}

	public void renderMesh(MeshRegistry.MeshRef mesh) {
		renderVertexStack(mesh.stack());
	}
	public ArrayList<MeshRegistry.MeshRef> recordings = new ArrayList<>();

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
		this.renderTarget.destroyBuffers();
		this.projectionMatrixBuffer.close();
	}

	public void render() {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("GooseGPU");

		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();

		GooseboyGpuRenderConsumer renderConsumer = new GooseboyGpuRenderConsumer(this);
		GooseboyGpuMemoryConsumer gpuMemoryConsumer = new GooseboyGpuMemoryConsumer(this.gpuMemory);

		for (GooseboyGpu.QueuedCommand queued : queuedCommands) {
			runCommand(
					queued.command(),
					queued.reader(),
					renderConsumer,
					gpuMemoryConsumer
			);
		}

		renderVertexStack(globalVertexStack);
		globalVertexStack.clear();
		queuedCommands.clear();

		matrix4fStack.popMatrix();

		profiler.pop();
	}

	public void runCommand(GooseboyGpu.GpuCommand command, GooseboyGpu.MemoryReadOffsetConsumer read,
						   GooseboyGpu.RenderConsumer render, GooseboyGpu.MemoryWriteOffsetConsumer write) {
		switch (command) {
			case Push, Pop -> {
				// TODO
			}
			case PushRecord -> {
				MeshRegistry.MeshRef mesh = meshRegistry.createMesh();
				recordings.add(mesh);
				write.writeInt(0, mesh.id());
			}
			case PopRecord -> recordings.removeLast();
			case DrawRecorded -> render.mesh(meshRegistry.getMesh(read.readInt(0)));
			case EmitVertex -> {
				MeshRegistry.MeshRef recording = recordings.getLast();
				float x = read.readFloat(0);
				float y = read.readFloat(4);
				float z = read.readFloat(8);
				float u = read.readFloat(12);
				float v = read.readFloat(16);

				if (recording == null) {
					// immediate-mode
					render.vertex(x, y, z, u, v);
				} else {
					// recommended instanced mode
					recording.stack()
							.push(new VertexStack.Vertex(
									x, y, z, u, v
							));
				}
			}
			case RegisterTexture -> {
				int width = read.readInt(0);
				int height = read.readInt(4);
				TextureRegistry.TextureRef texture = textureRegistry.createTexture(width, height);
				texture.set(read, 8, width * height * 4);
			}
			case BindTexture -> render.texture(textureRegistry.getTexture(read.readInt(0)));
		}
	}
}