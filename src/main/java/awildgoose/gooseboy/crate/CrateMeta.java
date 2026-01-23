package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.RawImage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrateMeta {
	public String name;
	public String entrypoint;
	public String description = "";
	public @Nullable String icon = null;
	public @Nullable String banner = null;
	public List<GooseboyCrate.Permission> permissions = new ArrayList<>();
	public List<GooseboyCrate.Permission> recommendedPermissions = new ArrayList<>();

	public transient byte[] binary; // is null after creation of GooseboyCrate
	public transient RawImage iconImage;
	public transient RawImage bannerImage;

	public int framebufferWidth = 320;
	public int framebufferHeight = 200;
	public boolean allowsMovement = false;
}
