package awildgoose.gooseboy.gpu.render;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.GooseboyClient;
import awildgoose.gooseboy.gpu.GpuConstants;
import awildgoose.gooseboy.gpu.command.GpuCommand;
import awildgoose.gooseboy.gpu.command.QueuedCommand;
import awildgoose.gooseboy.gpu.consumer.*;
import awildgoose.gooseboy.gpu.mesh.MeshRef;
import awildgoose.gooseboy.gpu.mesh.MeshRegistry;
import awildgoose.gooseboy.gpu.texture.TextureRef;
import awildgoose.gooseboy.gpu.texture.TextureRegistry;
import awildgoose.gooseboy.gpu.vertex.PrimitiveType;
import awildgoose.gooseboy.gpu.vertex.VertexStack;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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

@Environment(EnvType.CLIENT)
public class GooseboyGpuRenderer implements AutoCloseable {
	private static final int GPU_MATRIX_STACK_MAX = 64;

	public final GooseboyGpuCamera camera;
	public final ByteBuffer gpuMemory = ByteBuffer.allocateDirect(4192 * 4)
			.order(ByteOrder.LITTLE_ENDIAN);
	private final RenderSystem.AutoStorageIndexBuffer triangleIndices;
	private final RenderSystem.AutoStorageIndexBuffer quadIndices;
	private final TextureTarget renderTarget;
	private final CachedPerspectiveProjectionMatrixBuffer projectionMatrixBuffer;
	private final MeshRegistry meshRegistry = new MeshRegistry();
	private final TextureRegistry textureRegistry = new TextureRegistry();
	private final Matrix4fStack gpuModelStack = new Matrix4fStack(GPU_MATRIX_STACK_MAX);
	public final ArrayList<QueuedCommand> queuedCommands = new ArrayList<>();
	public final ArrayList<MeshRef> recordings = new ArrayList<>();
	public AbstractTexture boundTexture = null;
	private int gpuMatrixDepth = 0;
	private int frameStartGpuMatrixDepth = 0;

	public GooseboyGpuRenderer(int fbWidth, int fbHeight) {
		this.camera = new GooseboyGpuCamera(fbWidth, fbHeight);
		this.projectionMatrixBuffer = new CachedPerspectiveProjectionMatrixBuffer(
				"gooseboy_goosegpu", this.camera.near, this.camera.far);
		this.triangleIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.TRIANGLES);
		this.quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		this.renderTarget = new TextureTarget(
				"gooseboy_goosegpu_framebuffer",
				fbWidth,
				fbHeight,
				true
		);
	}

	public void renderVertexStack(VertexStack vertexStack, @Nullable TextureRef overrideTexture, PrimitiveType primitiveType) {
		GpuBuffer buffer = vertexStack.intoGpuBuffer();
		RenderSystem.AutoStorageIndexBuffer indices = switch (primitiveType) {
			case TRIANGLES -> this.triangleIndices;
			case QUADS -> this.quadIndices;
		};
		RenderPipeline pipeline = switch (primitiveType) {
			case TRIANGLES -> GooseboyClient.TRIANGLES_PIPELINE;
			case QUADS -> GooseboyClient.QUADS_PIPELINE;
		};
		int numIndices = switch (primitiveType) {
			case TRIANGLES -> vertexStack.size();
			case QUADS -> vertexStack.size() / 4;
		};
		int numIndicesToDraw = switch (primitiveType) {
			case TRIANGLES -> numIndices;
			case QUADS -> numIndices * 6;
		};

		if (buffer != null) {
			RenderSystem.backupProjectionMatrix();
			RenderSystem.setProjectionMatrix(
					this.projectionMatrixBuffer.getBuffer(
							this.renderTarget.width,
							this.renderTarget.height,
							this.camera.fovDegrees
					), ProjectionType.PERSPECTIVE);

			GpuTextureView colorView = this.renderTarget.getColorTextureView();
			GpuTextureView depthView = this.renderTarget.getDepthTextureView();

			AbstractTexture texture = overrideTexture == null ? this.boundTexture : overrideTexture.texture();

			if (this.boundTexture == null) {
				TextureManager textureManager = Minecraft.getInstance()
						.getTextureManager();
				texture = textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
				this.boundTexture = texture;
			}

			GpuBuffer indexBuffer = indices.getBuffer(numIndices);
			GpuBufferSlice transformSlice =
					this.camera.createTransformSlice(new Matrix4f(this.gpuModelStack), this.camera.getProjection());

			try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
							() -> "Gooseboy GooseGPU",
							colorView,
							OptionalInt.of(0),
							depthView,
							OptionalDouble.of(1.0)
					)) {
				renderPass.setPipeline(pipeline);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", transformSlice);
				renderPass.bindSampler("Sampler0", texture.getTextureView());
				renderPass.setIndexBuffer(indexBuffer, indices.type());
				renderPass.setVertexBuffer(0, buffer);
				renderPass.drawIndexed(0, 0, numIndicesToDraw, 1);
			}

			RenderSystem.restoreProjectionMatrix();
		}
	}

	public void renderMesh(MeshRef mesh) {
		this.renderVertexStack(mesh.stack(), mesh.texture, mesh.primitiveType());
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

		this.frameStartGpuMatrixDepth = this.gpuMatrixDepth;

		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix();

		GpuRenderConsumer renderConsumer = new GpuRenderConsumer(this);
		GpuMemoryWriter gpuMemoryWriter = new GpuMemoryWriter(this.gpuMemory);

		for (QueuedCommand queued : this.queuedCommands) {
			this.runCommand(
					queued.command(),
					queued.reader(),
					renderConsumer,
					gpuMemoryWriter
			);
		}

		this.queuedCommands.clear();

		if (this.gpuMatrixDepth != this.frameStartGpuMatrixDepth) {
			Gooseboy.LOGGER.warn(
					"GooseGPU: matrix stack leak! start={} end={}", this.frameStartGpuMatrixDepth,
					this.gpuMatrixDepth);

			int toPop = this.gpuMatrixDepth - this.frameStartGpuMatrixDepth;
			for (int i = 0; i < toPop; ++i) {
				this.gpuModelStack.popMatrix();
			}
			this.gpuMatrixDepth = this.frameStartGpuMatrixDepth;
		}

		modelView.popMatrix();

		profiler.pop();
	}

	public void setStatus(MemoryWriteConsumer write, int status) {
		write.writeInt(GpuConstants.GB_GPU_STATUS, status);
	}

	public void runCommand(GpuCommand command, MemoryReadConsumer read,
	                       RenderConsumer render, MemoryWriteConsumer write) {
		switch (command) {
			// Recording
			case PushRecord -> {
				byte primitiveType = read.readByte(0);
				MeshRef mesh = this.meshRegistry.createMesh(PrimitiveType.findPrimitiveById(primitiveType));
				this.recordings.add(mesh);
				write.writeInt(GpuConstants.GB_GPU_RECORD_ID, mesh.id());
			}
			case PopRecord -> {
				if (!this.recordings.isEmpty()) {
					this.recordings.removeLast();
				} else {
					this.setStatus(write, GpuConstants.GB_STATUS_NOT_RECORDING);
				}
			}
			case DrawRecorded -> render.mesh(this.meshRegistry.getMesh(read.readInt(0)));

			// Emit
			case EmitVertex -> {
				MeshRef recording = !this.recordings.isEmpty() ? this.recordings.getLast() : null;
				float x = read.readFloat(0);
				float y = read.readFloat(4);
				float z = read.readFloat(8);
				float u = read.readFloat(12);
				float v = read.readFloat(16);

				if (recording != null) {
					// recommended instanced mode
					recording.stack()
							.push(new VertexStack.Vertex(
									x, y, z, u, v
							));
				} else {
					// immediate-mode is not supported
					this.setStatus(write, GpuConstants.GB_STATUS_NOT_RECORDING);
				}
			}

			// Textures
			case RegisterTexture -> {
				int width = read.readInt(0);
				int height = read.readInt(4);

				if (width <= 0 || height <= 0 || width >= TextureRegistry.MAX_TEXTURE_WIDTH || height >= TextureRegistry.MAX_TEXTURE_HEIGHT) {
					this.setStatus(write, GpuConstants.GB_STATUS_BAD_TEXTURE_SIZE);
					return;
				}

				TextureRef texture = this.textureRegistry.createTexture(width, height);
				if (!texture.set(read, 8, width * height * 4))
					this.setStatus(write, GpuConstants.GB_STATUS_BAD_TEXTURE);
				write.writeInt(GpuConstants.GB_GPU_TEXTURE_ID, texture.id());
			}
			case BindTexture -> {
				MeshRef recording = !this.recordings.isEmpty() ? this.recordings.getLast() : null;
				int id = read.readInt(0);

				if (recording != null) {
					recording.texture = this.textureRegistry.getTexture(id);
				} else {
					// immediate-mode is not supported
					this.setStatus(write, GpuConstants.GB_STATUS_NOT_RECORDING);
				}
			}

			// Translations
			case Push -> {
				if (this.gpuMatrixDepth >= GPU_MATRIX_STACK_MAX) {
					this.setStatus(write, GpuConstants.GB_STATUS_MATRIX_TOO_BIG);
				} else {
					this.gpuModelStack.pushMatrix();
					this.gpuMatrixDepth++;
					write.writeInt(GpuConstants.GB_GPU_MATRIX_DEPTH, this.gpuMatrixDepth);
				}
			}
			case Pop -> {
				if (this.gpuMatrixDepth <= this.frameStartGpuMatrixDepth) {
					this.setStatus(write, GpuConstants.GB_STATUS_MATRIX_TOO_SMALL);
				} else {
					this.gpuModelStack.popMatrix();
					this.gpuMatrixDepth--;
					write.writeInt(GpuConstants.GB_GPU_MATRIX_DEPTH, 0);
				}
			}
			case Translate -> {
				float tx = read.readFloat(0);
				float ty = read.readFloat(4);
				float tz = read.readFloat(8);
				this.gpuModelStack.translate(tx, ty, tz);
			}
			case RotateAxis -> {
				float ax = read.readFloat(0);
				float ay = read.readFloat(4);
				float az = read.readFloat(8);
				float angle = read.readFloat(12);
				this.gpuModelStack.rotate(angle, ax, ay, az);
			}
			case RotateEuler -> {
				float yaw = read.readFloat(0);
				float pitch = read.readFloat(4);
				float roll = read.readFloat(8);
				this.gpuModelStack.rotateXYZ(yaw, pitch, roll);
			}
			case Scale -> {
				float sx = read.readFloat(0);
				float sy = read.readFloat(4);
				float sz = read.readFloat(8);
				this.gpuModelStack.scale(sx, sy, sz);
			}
			case LoadMatrix -> {
				float[] m = new float[16];
				for (int i = 0; i < 16; i++) m[i] = read.readFloat(i * 4);
				this.gpuModelStack.set(m);
			}
			case MulMatrix -> {
				float[] m = new float[16];
				for (int i = 0; i < 16; i++) m[i] = read.readFloat(i * 4);
				Matrix4f mat = new Matrix4f();
				mat.set(m);
				this.gpuModelStack.mul(mat);
			}
			case Identity -> this.gpuModelStack.identity();
		}
	}
}