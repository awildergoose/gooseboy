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

	public Vector3f cameraPos = new Vector3f();
	public Vector2f rotation = new Vector2f();
	private final int framebufferWidth;
	private final int framebufferHeight;
	public float fovDegrees = 70.0f;
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

	public void setYawPitch(float yaw, float pitch) {
		this.rotation.set(yaw, pitch);
	}

	public Vector2f getYawPitch() {
		return this.rotation;
	}

	public float getX() {
		return this.cameraPos.x;
	}

	public float getY() {
		return this.cameraPos.y;
	}

	public float getZ() {
		return this.cameraPos.z;
	}

	public Vector3f getCameraPos() {
		return this.cameraPos;
	}

	public void setCameraPos(Vector3f pos) {
		this.cameraPos.set(pos);
	}

	public float getYaw() {
		return this.rotation.x;
	}

	public void setYaw(float yawRadians) {
		this.rotation.x = (float) Math.IEEEremainder(yawRadians, Math.PI * 2.0);
	}

	public float getPitch() {
		return this.rotation.y;
	}

	public void setPitch(float pitchRadians) {
		final float MAX_PITCH = (float) Math.toRadians(89.0);
		this.rotation.y = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitchRadians));
	}

	public void setNearFar(float near, float far) {
		this.near = near;
		this.far = far;
		ensureFarPlane();
	}

	public float getFar() {
		return far;
	}

	public float getNear() {
		return near;
	}

	public void moveForward(float amount) {
		float yaw = this.rotation.x;

		cameraPos.x += (float) Math.sin(yaw) * amount;
		cameraPos.z -= (float) Math.cos(yaw) * amount;
	}

	public void moveRight(float amount) {
		float yaw = this.rotation.x;

		cameraPos.x += (float) Math.cos(yaw) * amount;
		cameraPos.z += (float) Math.sin(yaw) * amount;
	}

	public void moveUp(float amount) {
		cameraPos.y += amount;
	}

	public GpuBufferSlice createTransformSlice() {
		return createTransformSlice(new Matrix4f().identity());
	}

	public GpuBufferSlice createTransformSlice(Matrix4f model) {
		Matrix4f view = new Matrix4f()
				.rotateY(rotation.x)
				.rotateX(-rotation.y)
				.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
				.translate(0.0f, 0.0f, GUI_Z_OFFSET);
		Matrix4f modelView = new Matrix4f(view).mul(model);
		Matrix4f projection = new Matrix4f()
				.perspective(
						(float) Math.toRadians(fovDegrees),
						aspect(),
						near,
						far
				);

		return RenderSystem.getDynamicUniforms()
				.writeTransform(
						modelView,
						new Vector4f(1, 1, 1, 1),
						new Vector3f(),
						projection,
						0.0f
				);
	}
}
