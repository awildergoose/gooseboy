package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.Gooseboy;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GooseboyGpuCamera {
	private static final float GUI_Z_OFFSET = -11000.0f;

	private final Vector3f cameraPos = new Vector3f();
	private final Vector2f rotation = new Vector2f();
	private final int framebufferWidth;
	private final int framebufferHeight;
	private float fovDegrees = 70.0f;
	private float near = 0.1f;
	private float far = 20000.0f;

	public GooseboyGpuCamera() {
		this.framebufferWidth = Gooseboy.FRAMEBUFFER_WIDTH;
		this.framebufferHeight = Gooseboy.FRAMEBUFFER_HEIGHT;
		ensureFarPlane();
	}

	private void ensureFarPlane() {
		float needed = Math.abs(GUI_Z_OFFSET) + 100.0f;
		if (this.far < needed) this.far = needed + 1000.0f;
	}

	private float aspect() {
		return (float) this.framebufferWidth / (float) this.framebufferHeight;
	}

	public void setCameraPos(float x, float y, float z) {
		this.cameraPos.set(x, y, z);
	}

	public float getYaw() {
		return this.rotation.x;
	}

	public void setYaw(float yawRadians) {
		this.rotation.x = yawRadians;
	}

	public float getPitch() {
		return this.rotation.y;
	}

	public void setPitch(float pitchRadians) {
		this.rotation.y = pitchRadians;
	}

	public void setFovDegrees(float fovDegrees) {
		this.fovDegrees = fovDegrees;
	}

	public void setNearFar(float near, float far) {
		this.near = near;
		this.far = far;
		ensureFarPlane();
	}

	public GpuBufferSlice createTransformSlice() {
		return createTransformSlice(new Matrix4f().identity());
	}

	public GpuBufferSlice createTransformSlice(Matrix4f model) {
		Matrix4f view = new Matrix4f()
				.translate(0.0f, 0.0f, GUI_Z_OFFSET)
				.rotateX(this.getPitch())
				.rotateY(this.getYaw())
				.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f modelView = new Matrix4f(view).mul(model);
		Matrix4f projection = new Matrix4f()
				.perspective(
						(float) Math.toRadians(this.fovDegrees),
						this.aspect(),
						this.near,
						this.far
				);

		Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		Vector3f dummyVec = new Vector3f();

		return RenderSystem.getDynamicUniforms()
				.writeTransform(modelView, color, dummyVec, projection, 0.0f);
	}
}
