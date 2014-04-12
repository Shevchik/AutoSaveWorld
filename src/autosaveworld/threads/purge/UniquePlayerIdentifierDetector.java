package autosaveworld.threads.purge;

import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.Server;

public class UniquePlayerIdentifierDetector {
	
	public static UniquePlayerIdentifierType getUniquePlayerIdentifierType() {
		try {
			Server server = Bukkit.getServer();
			@SuppressWarnings("deprecation")
			Class<?> craftofflineplayer = Bukkit.getOfflinePlayer("fakeautopurgeplayer").getClass();
			craftofflineplayer.getConstructor(server.getClass(), GameProfile.class);
			return UniquePlayerIdentifierType.UUID;
		} catch (Exception e) {
		}
		return UniquePlayerIdentifierType.NAME;
	}

	public static enum UniquePlayerIdentifierType {
		NAME, UUID;
	}

}
