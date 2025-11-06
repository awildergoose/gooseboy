package awildgoose.gooseboy.crate;

import awildgoose.gooseboy.Gooseboy;
import com.dylibso.chicory.runtime.Instance;

import java.util.ArrayList;

public class CrateUtils {
	private static final ArrayList<GooseboyCrate.Permission> permissionsWarned = new ArrayList<>();

	public static boolean doesNotHavePermission(Instance instance, GooseboyCrate.Permission permission) {
		if (Gooseboy.getCrate(instance).permissions.contains(permission)) {
			return false;
		}

		if (!permissionsWarned.contains(permission)) {
			permissionsWarned.add(permission);
			Gooseboy.ccb.warnPermission(permission);
		}

		return true;
	}

	public static void clearWarns() {
		permissionsWarned.clear();
	}
}
