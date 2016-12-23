package autosaveworld.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;

public class BukkitUtils {

	@SuppressWarnings("unchecked")
	public static Collection<Player> getOnlinePlayers() {
		try {
			Method method = ReflectionUtils.getMethod(Bukkit.class, "getOnlinePlayers", 0);
			if (method.getReturnType() == Collection.class) {
				return (Collection<Player>) method.invoke(null);
			} else if (method.getReturnType().isArray()) {
				return Arrays.asList((Player[]) method.invoke(null));
			} else {
				throw new RuntimeException("Return type "+method.getReturnType()+" is not supported");
			}
		} catch (Throwable t) {
			ReflectionUtils.throwException(t);
			return null;
		}
	}

	public static void dispatchCommandAsConsole(String command) {
		ServerCommandEvent event = new ServerCommandEvent(Bukkit.getConsoleSender(), command);
		Bukkit.getPluginManager().callEvent(event);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getCommand());
	}

}
