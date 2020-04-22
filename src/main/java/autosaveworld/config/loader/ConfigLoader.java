/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package autosaveworld.config.loader;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;

import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.loader.transform.YamlTransform;
import autosaveworld.core.logging.MessageLogger;

public class ConfigLoader {

	public static void load(Config config) {
		try {
			YamlConfiguration yconfig = new YamlConfiguration();
			try {
				yconfig.load(config.getFile());
			} catch (FileNotFoundException e) {
			}
			for (Field field : config.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				ConfigOption option = field.getAnnotation(ConfigOption.class);
				if (option != null) {
					YamlTransform transform = option.transform().newInstance();
					String path = option.path();
					Object newvalue = field.get(config);
					if (yconfig.contains(path)) {
						newvalue = transform.fromYaml(yconfig.get(path));
					} else {
						for (String legacyPath : option.legacypath()) {
							if (yconfig.contains(legacyPath)) {
								newvalue = transform.fromYaml(yconfig.get(legacyPath));
								break;
							}
						}
					}
					option.postload().newInstance().postLoad(newvalue);
					field.set(config, newvalue);
				}
			}
		} catch (Throwable t) {
			MessageLogger.exception("Unable to load config "+config.getClass().getSimpleName() + ", defaulting to already configured or default values", t);
		}
	}

	public static void save(Config config) {
		try {
			YamlConfiguration yconfig = new YamlConfiguration();
			for (Field field : config.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				ConfigOption option = field.getAnnotation(ConfigOption.class);
				if (option != null) {
					YamlTransform transform = option.transform().newInstance();
					yconfig.set(option.path(), transform.toYaml(field.get(config)));
				}
			}
			yconfig.save(config.getFile());
		} catch (Throwable t) {
			MessageLogger.exception("Unable to save config "+config.getClass().getSimpleName(), t);
		}
	}

	public static void loadAndSave(Config config) {
		load(config);
		save(config);
	}

}
