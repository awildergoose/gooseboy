package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.Gooseboy;
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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
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
	private static final int GPU_MATRIX_STACK_MAX = 64;

	public final GooseboyGpuCamera camera = new GooseboyGpuCamera();

	private final RenderSystem.AutoStorageIndexBuffer indices;
	private final TextureTarget renderTarget;
	private final CachedPerspectiveProjectionMatrixBuffer projectionMatrixBuffer = new CachedPerspectiveProjectionMatrixBuffer(
			"gooseboy_goosegpu", camera.near, camera.far);

	private final MeshRegistry meshRegistry = new MeshRegistry();
	private final TextureRegistry textureRegistry = new TextureRegistry();

	public final ByteBuffer gpuMemory = ByteBuffer.allocateDirect(4192 * 4)
			.order(ByteOrder.LITTLE_ENDIAN);
	public ArrayList<GooseboyGpu.QueuedCommand> queuedCommands = new ArrayList<>();
	public VertexStack globalVertexStack = new VertexStack();
	private final Matrix4fStack gpuModelStack = new Matrix4fStack(GPU_MATRIX_STACK_MAX);
	public ArrayList<MeshRegistry.MeshRef> recordings = new ArrayList<>();
	public AbstractTexture boundTexture = null;
	private int gpuMatrixDepth = 0;
	private int frameStartGpuMatrixDepth = 0;

	public GooseboyGpuRenderer() {
		this.indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES);
		this.renderTarget = new TextureTarget(
				"gooseboy_goosegpu_framebuffer",
				FRAMEBUFFER_WIDTH,
				FRAMEBUFFER_HEIGHT,
				true
		);
	}

	public void renderVertexStack(VertexStack vertexStack, @Nullable TextureRegistry.TextureRef overrideTexture) {
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

			AbstractTexture texture = overrideTexture == null ? boundTexture : overrideTexture.texture;

			if (boundTexture == null) {
				TextureManager textureManager = Minecraft.getInstance()
						.getTextureManager();
				texture = textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
				boundTexture = texture;
			}

			int indexCount = vertexStack.size();
			GpuBuffer indexBuffer = this.indices.getBuffer(indexCount);
			GpuBufferSlice transformSlice =
					this.camera.createTransformSlice(new Matrix4f(gpuModelStack), camera.getProjection());

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
		renderVertexStack(mesh.stack(), mesh.texture);
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
		this.renderTarget.destroyBuffers();
		this.projectionMatrixBuffer.close();
	}

	public void render() {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("GooseGPU");

		frameStartGpuMatrixDepth = gpuMatrixDepth;

		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix();

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

		renderVertexStack(globalVertexStack, null);
		globalVertexStack.clear();
		queuedCommands.clear();

		if (gpuMatrixDepth != frameStartGpuMatrixDepth) {
			Gooseboy.LOGGER.warn("GooseGPU: matrix stack leak! start={} end={}", frameStartGpuMatrixDepth,
								 gpuMatrixDepth);

			int toPop = gpuMatrixDepth - frameStartGpuMatrixDepth;
			for (int i = 0; i < toPop; ++i) {
				gpuModelStack.popMatrix();
			}
			gpuMatrixDepth = frameStartGpuMatrixDepth;
		}

		modelView.popMatrix();

		profiler.pop();
	}

	public void runCommand(GooseboyGpu.GpuCommand command, GooseboyGpu.MemoryReadOffsetConsumer read,
						   GooseboyGpu.RenderConsumer render, GooseboyGpu.MemoryWriteOffsetConsumer write) {
		switch (command) {
			// Recording
			case PushRecord -> {
				MeshRegistry.MeshRef mesh = meshRegistry.createMesh();
				recordings.add(mesh);
				write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_RECORD_ID, mesh.id());
			}
			case PopRecord -> recordings.removeLast();
			case DrawRecorded -> render.mesh(meshRegistry.getMesh(read.readInt(0)));

			// Emit
			case EmitVertex -> {
				MeshRegistry.MeshRef recording = recordings.size() > 0 ? recordings.getLast() : null;
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

			// Textures
			case RegisterTexture -> {
				int width = read.readInt(0);
				int height = read.readInt(4);
				TextureRegistry.TextureRef texture = textureRegistry.createTexture(width, height);
				texture.set(read, 8, width * height * 4);
				write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_TEXTURE_ID, texture.id);
			}
			case BindTexture -> {
				MeshRegistry.MeshRef recording = recordings.size() > 0 ? recordings.getLast() : null;
				int id = read.readInt(0);

				if (recording == null) {
					render.texture(textureRegistry.getTexture(id));
				} else {
					recording.texture = textureRegistry.getTexture(id);
				}
			}

			// Translations
			case Push -> {
				if (gpuMatrixDepth >= GPU_MATRIX_STACK_MAX) {
					write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_STATUS, -1);
				} else {
					gpuModelStack.pushMatrix();
					gpuMatrixDepth++;
					write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_MATRIX_DEPTH, gpuMatrixDepth);
				}
			}
			case Pop -> {
				if (gpuMatrixDepth <= frameStartGpuMatrixDepth) {
					Gooseboy.LOGGER.warn("GooseGPU: attempted pop below frame start");
					write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_STATUS, -1);
				} else {
					gpuModelStack.popMatrix();
					gpuMatrixDepth--;
					write.writeInt(GooseboyGpuMemoryConstants.GB_GPU_MATRIX_DEPTH, 0);
				}
			}
			case Translate -> {
				float tx = read.readFloat(0);
				float ty = read.readFloat(4);
				float tz = read.readFloat(8);
				gpuModelStack.translate(tx, ty, tz);
			}
			case RotateAxis -> {
				float ax = read.readFloat(0);
				float ay = read.readFloat(4);
				float az = read.readFloat(8);
				float angle = read.readFloat(12);
				gpuModelStack.rotate(angle, ax, ay, az);
			}
			case RotateEuler -> {
				float yaw = read.readFloat(0);
				float pitch = read.readFloat(4);
				float roll = read.readFloat(8);
				gpuModelStack.rotateXYZ(yaw, pitch, roll);
			}
			case Scale -> {
				float sx = read.readFloat(0);
				float sy = read.readFloat(4);
				float sz = read.readFloat(8);
				gpuModelStack.scale(sx, sy, sz);
			}
			case LoadMatrix -> {
				float[] m = new float[16];
				for (int i = 0; i < 16; i++) m[i] = read.readFloat(i * 4);
				gpuModelStack.set(m);
			}
			case MulMatrix -> {
				float[] m = new float[16];
				for (int i = 0; i < 16; i++) m[i] = read.readFloat(i * 4);
				Matrix4f mat = new Matrix4f();
				mat.set(m);
				gpuModelStack.mul(mat);
			}
			case Identity -> gpuModelStack.identity();
		}
	}
}