package awildgoose.gooseboy.gpu;

import com.dylibso.chicory.runtime.Memory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

public class TextureRegistry {
	private static final ArrayList<TextureRef> textures = new ArrayList<>();
	private static int lastTextureId = 0;

	public static TextureRef createTexture() {
		TextureRef ref = new TextureRef(new UnnamedDynamicTexture(), lastTextureId++);
		textures.add(ref);
		return ref;
	}

	public static @Nullable UnnamedDynamicTexture getTexture(int id) {
		Optional<TextureRef> ref = textures.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.map(TextureRef::texture)
				.orElse(null);
	}

	public record TextureRef(UnnamedDynamicTexture texture, int id) {
		public void set(Memory memory, int ptr, int len) {
			ByteBuffer pixels = ByteBuffer.wrap(memory.readBytes(ptr, len));
			MemoryUtil.memCopy(
					MemoryUtil.memAddress(pixels),
					this.texture.getPixels()
							.getPointer(),
					len);
		}
	}
}
