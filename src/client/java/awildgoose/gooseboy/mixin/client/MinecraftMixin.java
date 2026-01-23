package awildgoose.gooseboy.mixin.client;

import awildgoose.gooseboy.screen.renderer.CrateRendererScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@Environment(EnvType.CLIENT)
public abstract class MinecraftMixin {
	@Shadow
	private void handleKeybinds() {
		throw new AssertionError("This should never happen");
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"
			),
			locals = org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private void gooseboy$preKeybindingsCheck(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen instanceof CrateRendererScreen<?> screen && screen.allowsMovement) {
			this.handleKeybinds();
			if (minecraft.missTime > 0)
				minecraft.missTime--;
		}
	}
}
