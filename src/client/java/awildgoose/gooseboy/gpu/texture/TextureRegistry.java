package awildgoose.gooseboy.gpu.texture;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class TextureRegistry {
	public static final int MAX_TEXTURE_WIDTH = 256;
	public static final int MAX_TEXTURE_HEIGHT = 256;

	private final ArrayList<TextureRef> textures = new ArrayList<>();
	private int lastTextureId = 0;

	public TextureRef createTexture(int width, int height) {
		TextureRef ref = new TextureRef(new UnnamedDynamicTexture(width, height), lastTextureId++);
		textures.add(ref);
		return ref;
	}

	public @Nullable TextureRef getTexture(int id) {
		Optional<TextureRef> ref = textures.stream()
				.filter(f -> f.id == id)
				.findFirst();
		return ref.orElse(null);
	}
}
