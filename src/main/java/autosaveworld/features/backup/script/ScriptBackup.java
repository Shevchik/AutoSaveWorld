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

package autosaveworld.features.backup.script;

import java.io.File;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.Backup;

public class ScriptBackup extends Backup {

	public ScriptBackup() {
		super("Script");
	}

	public void performBackup() {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		for (String scriptpath : config.backupScriptPaths) {
			File scriptfile = new File(scriptpath);
			if (!scriptpath.isEmpty() && scriptfile.exists() && scriptfile.isFile()) {
				MessageLogger.debug("Executing script " + scriptfile.getAbsolutePath());
				final Process p;
				ProcessBuilder pb = new ProcessBuilder();
				pb.command(scriptfile.getAbsolutePath());
				pb.inheritIO();
				try {
					p = pb.start();
					p.waitFor();
				} catch (Exception e) {
					MessageLogger.exception("Exception occured while executing script " + scriptpath, e);
				}
			} else {
				MessageLogger.debug("Script path is invalid");
			}
		}
	}

}
