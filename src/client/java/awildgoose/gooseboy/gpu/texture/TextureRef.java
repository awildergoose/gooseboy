package awildgoose.gooseboy.gpu.texture;

import awildgoose.gooseboy.Gooseboy;
import awildgoose.gooseboy.gpu.consumer.MemoryReadConsumer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class TextureRef {
	public final int id;
	public UnnamedDynamicTexture texture;

	public TextureRef(@NotNull UnnamedDynamicTexture texture, int id) {
		this.texture = texture;
		this.id = id;
	}

	public boolean set(MemoryReadConsumer memory, int ptr, int len) {
		final byte[] src = memory.readBytes(ptr, len);

		if (src == null || src.length == 0) {
			Gooseboy.LOGGER.warn("failed to load texture, src is empty id={} ptr={} wanted={}", id, ptr, len);
			return false;
		}

		ByteBuffer tmp = null;

		try {
			try {
				tmp = MemoryUtil.memAlloc(src.length);
			} catch (Throwable t) {
				Gooseboy.LOGGER.error("failed to allocate for texture for id={} len={} -> {}",
									  id, src.length, t);
				return false;
			}

			tmp.put(src, 0, src.length);
			tmp.flip();

			final long dstPtr = this.texture.getPixels()
					.getPointer();

			try {
				MemoryUtil.memCopy(MemoryUtil.memAddress(tmp), dstPtr, src.length);
			} catch (Throwable t) {
				Gooseboy.LOGGER.error("failed to copy texture for id={} len={} -> {}", id, src.length, t);
				return false;
			}

			this.texture.upload();
		} finally {
			try {
				MemoryUtil.memFree(tmp);
			} catch (Throwable t) {
				Gooseboy.LOGGER.error("failed to free texture for id={} -> {}", id, t);
			}
		}

		return true;
	}
}
