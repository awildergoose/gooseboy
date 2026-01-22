package awildgoose.gooseboy.gpu;

import awildgoose.gooseboy.Gooseboy;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class GooseboyGpuCamera {
	public final Vector3f position = new Vector3f();
	public final Vector2f rotation = new Vector2f();
	private final int framebufferWidth;
	private final int framebufferHeight;
	public float fovDegrees = 70.0f;
	public float near = 0.1f;
	public float far = 20000.0f;

	public GooseboyGpuCamera() {
		this.framebufferWidth = Gooseboy.FRAMEBUFFER_WIDTH;
		this.framebufferHeight = Gooseboy.FRAMEBUFFER_HEIGHT;
	}

	private float aspect() {
		return (float) this.framebufferWidth / (float) this.framebufferHeight;
	}

	public GpuBufferSlice createTransformSlice() {
		return createTransformSlice(new Matrix4f().identity());
	}

	public GpuBufferSlice createTransformSlice(Matrix4f projection) {
		return createTransformSlice(new Matrix4f().identity(), projection);
	}

	public Matrix4f getProjection() {
		return new Matrix4f().perspective(
				(float) Math.toRadians(fovDegrees),
				aspect(),
				near,
				far
		);
	}

	public GpuBufferSlice createTransformSlice(Matrix4f model, Matrix4f projection) {
		Matrix4f view = new Matrix4f()
				.identity()
				.rotateX(-getPitch())
				.rotateY(-getYaw())
				.translate(-getX(), -getY(), -getZ());
		Matrix4f modelView = new Matrix4f(view).mul(model);

		return RenderSystem.getDynamicUniforms()
				.writeTransform(
						modelView,
						new Vector4f(1, 1, 1, 1),
						new Vector3f(),
						projection,
						0.0f
				);
	}

	//#region utils
	public Vector3f getForwardVector() {
		float yaw = rotation.x;
		float pitch = rotation.y;

		float cosPitch = (float) Math.cos(pitch);
		float sinPitch = (float) Math.sin(pitch);
		float cosYaw = (float) Math.cos(yaw);
		float sinYaw = (float) Math.sin(yaw);

		return new Vector3f(
				-sinYaw * cosPitch,
				sinPitch,
				-cosYaw * cosPitch
		);
	}

	public Vector3f getRightVector() {
		Vector3f forward = getForwardVector();
		Vector3f up = new Vector3f(0, 1, 0);

		return forward.cross(up, new Vector3f())
				.normalize();
	}

	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
	}

	public void setYawPitch(float yaw, float pitch) {
		this.rotation.set(yaw, pitch);
	}

	public Vector2f getYawPitch() {
		return this.rotation;
	}

	public float getX() {
		return this.position.x;
	}

	public float getY() {
		return this.position.y;
	}

	public float getZ() {
		return this.position.z;
	}

	public Vector3f getPosition() {
		return this.position;
	}

	public void setPosition(Vector3f pos) {
		this.position.set(pos);
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
	//#endregion
}
