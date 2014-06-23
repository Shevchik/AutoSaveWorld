package autosaveworld.zlibs.com.fasterxml.jackson.core.json;

import autosaveworld.zlibs.com.fasterxml.jackson.core.Version;
import autosaveworld.zlibs.com.fasterxml.jackson.core.Versioned;
import autosaveworld.zlibs.com.fasterxml.jackson.core.util.VersionUtil;

public final class PackageVersion implements Versioned {

	public static final Version VERSION = VersionUtil.parseVersion("2.2.3", "com.fasterxml.jackson.core", "jackson-core");

	@Override
	public Version version() {
		return VERSION;
	}

}
