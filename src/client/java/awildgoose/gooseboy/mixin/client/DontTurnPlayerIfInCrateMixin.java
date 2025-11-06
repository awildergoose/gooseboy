package awildgoose.gooseboy.mixin.client;

import awildgoose.gooseboy.WasmInputManager;
import awildgoose.gooseboy.screen.WasmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class DontTurnPlayerIfInCrateMixin {
	@Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
	private void turnPlayer(double d, CallbackInfo ci) {
		//noinspection ReferenceToMixin
		var mouseHandler = (MouseHandlerAccessor)Minecraft.getInstance().mouseHandler;
		WasmInputManager.LAST_ACCUMULATED_MOUSE_X = mouseHandler.gooseboy$getAccumulatedDX();
		WasmInputManager.LAST_ACCUMULATED_MOUSE_Y = mouseHandler.gooseboy$getAccumulatedDY();

		if (Minecraft.getInstance().screen instanceof WasmScreen) {
			ci.cancel();
		}
	}
}
