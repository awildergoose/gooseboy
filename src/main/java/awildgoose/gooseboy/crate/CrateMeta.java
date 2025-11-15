package awildgoose.gooseboy.crate;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrateMeta {
	public String name;
	public String entrypoint;
	public String description = "";
	public @Nullable String icon = null;
	public @Nullable String banner = null;
	public List<GooseboyCrate.Permission> permissions;

	public transient byte[] binary;
	public transient byte[] rawIcon;
	public transient byte[] rawBanner;
}
