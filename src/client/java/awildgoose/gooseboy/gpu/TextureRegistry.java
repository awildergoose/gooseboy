package awildgoose.gooseboy.gpu;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

public class TextureRegistry {
	private static final ArrayList<TextureRef> textures = new ArrayList<>();
	private static int lastTextureId = 0;

	public static TextureRef createTexture(int width, int height) {
		TextureRef ref = new TextureRef(new UnnamedDynamicTexture(width, height), lastTextureId++);
		textures.add(ref);
		return ref;
	}

	public static @Nullable TextureRef getTexture(int id) {
		Optional<TextureRef> ref = textures.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.orElse(null);
	}

	public static final class TextureRef {
		public final int id;
		public UnnamedDynamicTexture texture;

		public TextureRef(UnnamedDynamicTexture texture, int id) {
			this.texture = texture;
			this.id = id;
		}

		public void set(GooseboyGpu.MemoryReadOffsetConsumer memory, int ptr, int len) {
			byte[] src = memory.readBytes(ptr, len);

			ByteBuffer tmp = MemoryUtil.memAlloc(len);
			try {
				tmp.put(src)
						.flip();

				MemoryUtil.memCopy(
						MemoryUtil.memAddress(tmp),
						this.texture.getPixels()
								.getPointer(),
						len
				);
			} finally {
				MemoryUtil.memFree(tmp);
			}

			this.texture.upload();
		}
	}
}
