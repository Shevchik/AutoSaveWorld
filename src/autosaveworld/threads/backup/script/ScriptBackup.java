/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.threads.backup.script;

import java.io.File;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class ScriptBackup {

	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	public ScriptBackup(AutoSaveWorld plugin, AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}


	public void performBackup()
	{
		for (String scriptpath : config.scriptbackupscriptpaths)
		{
			File scriptfile = new File(scriptpath);
			if (!scriptpath.isEmpty() && scriptfile.exists())
			{
				plugin.debug("Executing script "+ scriptfile.getAbsolutePath());
				final Process p;
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(scriptfile.getAbsolutePath());
				pb.inheritIO();
				try {
					p = pb.start();
					p.waitFor();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
			{
				plugin.debug("Script path is invalid");
			}
		}
	}


}
