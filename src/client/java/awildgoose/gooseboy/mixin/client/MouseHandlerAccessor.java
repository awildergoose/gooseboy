package awildgoose.gooseboy.mixin.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Invoker("onMove")
	void gooseboy$onMove(long handle, double x, double y);
	@Accessor("mouseGrabbed")
	boolean gooseboy$isMouseGrabbed();
	@Accessor("mouseGrabbed")
	void gooseboy$setMouseGrabbed(boolean grabbed);
}
