package awildgoose.gooseboy.mixin.client;

import awildgoose.gooseboy.screen.renderer.CrateRendererScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
@Environment(EnvType.CLIENT)
public abstract class MouseHandlerMixin {
	@Shadow private boolean isLeftPressed;
	@Shadow private boolean isMiddlePressed;
	@Shadow private boolean isRightPressed;
	@Shadow
	@Final
	private ScrollWheelHandler scrollWheelHandler;

	@Shadow
	private MouseButtonInfo simulateRightClick(MouseButtonInfo mouseButtonInfo, boolean bl) {
		throw new AssertionError("This should never happen");
	}

	@Inject(at = @At("TAIL"), method = "onButton")
	private void onButton(long l, MouseButtonInfo mouseButtonInfo, int i, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();

		if (l == window.handle() && mc.screen instanceof CrateRendererScreen<?> screen && screen.allowsMovement) {
			boolean bl = i != 0;

			MouseButtonInfo actual = this.simulateRightClick(mouseButtonInfo, bl);

			if (actual.button() == 0) this.isLeftPressed = bl;
			else if (actual.button() == 2) this.isMiddlePressed = bl;
			else if (actual.button() == 1) this.isRightPressed = bl;

			InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(actual.button());
			KeyMapping.set(key, bl);
			if (bl) KeyMapping.click(key);
		}
	}

	@Inject(at = @At("TAIL"), method = "onScroll")
	private void onScroll(long l, double d, double e, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		if (l == minecraft.getWindow()
				.handle()) {
			if (minecraft.screen instanceof CrateRendererScreen<?> screen && screen.allowsMovement && minecraft.player != null) {
				boolean bl = minecraft.options.discreteMouseScroll()
						.get();
				double f = minecraft.options.mouseWheelSensitivity()
						.get();
				double g = (bl ? Math.signum(d) : d) * f;
				double h = (bl ? Math.signum(e) : e) * f;

				Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(g, h);
				if (vector2i.x == 0 && vector2i.y == 0) {
					return;
				}

				int k = vector2i.y == 0 ? -vector2i.x : vector2i.y;
				if (minecraft.player.isSpectator()) {
					if (minecraft.gui.getSpectatorGui()
							.isMenuActive()) {
						minecraft.gui.getSpectatorGui()
								.onMouseScrolled(-k);
					} else {
						float m = Mth.clamp(minecraft.player.getAbilities()
													.getFlyingSpeed() + vector2i.y * 0.005F, 0.0F, 0.2F);
						minecraft.player.getAbilities()
								.setFlyingSpeed(m);
					}
				} else {
					Inventory inventory = minecraft.player.getInventory();
					inventory.setSelectedSlot(
							ScrollWheelHandler.getNextScrollWheelSelection(
									k, inventory.getSelectedSlot(), Inventory.getSelectionSize()));
				}
			}
		}
	}
}
