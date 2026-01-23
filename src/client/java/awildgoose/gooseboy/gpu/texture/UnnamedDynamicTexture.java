package awildgoose.gooseboy.gpu.texture;

import awildgoose.gooseboy.Gooseboy;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class UnnamedDynamicTexture extends AbstractTexture implements Dumpable {
	private NativeImage pixels;

	public UnnamedDynamicTexture(int width, int height) {
		this.pixels = new NativeImage(width, height, false);
		this.createTexture();
		this.upload();
	}

	private void createTexture() {
		GpuDevice gpuDevice = RenderSystem.getDevice();
		this.texture = gpuDevice.createTexture((Supplier<String>) null, 5, TextureFormat.RGBA8,
											   this.pixels.getWidth(), this.pixels.getHeight(), 1, 1);
		this.texture.setTextureFilter(FilterMode.NEAREST, false);
		this.textureView = gpuDevice.createTextureView(this.texture);
	}

	public void upload() {
		if (this.pixels != null && this.texture != null) {
			RenderSystem.getDevice()
					.createCommandEncoder()
					.writeToTexture(this.texture, this.pixels);
		} else {
			Gooseboy.LOGGER.warn(
					"Trying to upload disposed texture {}", this.getTexture()
							.getLabel());
		}
	}

	public NativeImage getPixels() {
		return this.pixels;
	}

	@Override
	public void close() {
		if (this.pixels != null) {
			this.pixels.close();
			this.pixels = null;
		}

		super.close();
	}

	@Override
	public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
		if (this.pixels != null) {
			String string = resourceLocation.toDebugFileName() + ".png";
			Path path2 = path.resolve(string);
			this.pixels.writeToFile(path2);
		}
	}
}
