package awildgoose.gooseboy;

import awildgoose.gooseboy.screen.WasmScreen;

public class ClientCommonBridgeImpl implements ClientCommonBridge {
	@Override
	public void clear(int color) {
		WasmScreen.INSTANCE.clear(color);
	}
}
