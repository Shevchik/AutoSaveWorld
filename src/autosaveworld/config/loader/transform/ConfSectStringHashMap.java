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

package autosaveworld.config.loader.transform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfSectStringHashMap implements YamlTransform {

	@SuppressWarnings("unchecked")
	@Override
	public Object toYaml(Object obj) {
		Map<String, List<String>> map = (Map<String, List<String>>) obj;
		MemorySection section = new YamlConfiguration();
		for (Entry<String, List<String>> entry : map.entrySet()) {
			section.set(entry.getKey(), entry.getValue());
		}
		return section;
	}

	@Override
	public Object fromYaml(Object obj) {
		ConfigurationSection sect = (ConfigurationSection) obj;
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		for (String key : sect.getKeys(false)) {
			map.put(key, sect.getStringList(key));
		}
		return map;
	}

}
