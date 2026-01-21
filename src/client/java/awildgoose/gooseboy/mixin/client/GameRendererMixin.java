package awildgoose.gooseboy.mixin.client;

import awildgoose.gooseboy.screen.CenteredCrateScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
					shift = At.Shift.AFTER
			)
	)
	private void gooseboy$renderBeforeGui(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof CenteredCrateScreen crateScreen) {
			CenteredCrateScreen.Layout layout = CenteredCrateScreen.Layout.forSize(screen.width, screen.height);
			crateScreen.painter.render3D(layout.fbX(), layout.fbY());
		}
	}
}
