package awildgoose.gooseboy.mixin.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Accessor("xpos")
	void gooseboy$setXPos(double value);
	@Accessor("ypos")
	void gooseboy$setYPos(double value);
	@Accessor("mouseGrabbed")
	boolean gooseboy$isMouseGrabbed();
	@Accessor("mouseGrabbed")
	void gooseboy$setMouseGrabbed(boolean grabbed);
	@Accessor("accumulatedDX")
	double gooseboy$getAccumulatedDX();
	@Accessor("accumulatedDY")
	double gooseboy$getAccumulatedDY();
}
