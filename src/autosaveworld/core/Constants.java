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

package autosaveworld.core;

import java.io.File;

public class Constants {

	public Constants(AutoSaveWorld plugin)
	{
		pluginfolder = plugin.getDataFolder().getPath()+File.separator;
	}
	
	//main
	private String pluginfolder;
	public String getPluginFolder()
	{
		return pluginfolder;
	}
	
	//config
	private String configfile = "config.yml";
	public String getConfigPath()
	{
		return getPluginFolder()+configfile;
	}
	private String configmsgfile = "configmsg.yml";
	public String getConfigMSGPath()
	{
		return getPluginFolder()+configmsgfile;
	}

	//counter preserve files
	private String backuppreserverfile = "backupintervalpreserve.yml";
	public String getBackupIntervalPreservePath()
	{
		return getPluginFolder()+backuppreserverfile;
	}
	
	//backup
	private String backuptempfolder = "BackupTemp"+File.separator;
	public String getBackupTempFolder()
	{
		return getPluginFolder()+backuptempfolder;
	}

	//worldregen
	private String worldregentempfolder = "WorldRegenTemp"+File.separator;
	private String worldnamefile = "wname.yml";
	private String shouldpastefile = "shouldpaste";
	private String wgtempfolder = "WG"+File.separator;
	private String factionstempfolder = "Factions"+File.separator;	
	private String griefpreventiontempfolder = "GP"+File.separator;
	private String townytempfolder = "Towny"+File.separator;
	public String getWorldRegenTempFolder()
	{
		return getPluginFolder()+worldregentempfolder;
	}
	public String getWorldnameFile()
	{
		return getWorldRegenTempFolder()+worldnamefile;
	}
	public String getShouldpasteFile()
	{
		return getWorldRegenTempFolder()+shouldpastefile;
	}
	public String getWGTempFolder()
	{
		return getWorldRegenTempFolder()+wgtempfolder;
	}
	public String getFactionsTempFolder()
	{
		return getWorldRegenTempFolder()+factionstempfolder;
	}
	public String getGPTempFolder()
	{
		return getWorldRegenTempFolder()+griefpreventiontempfolder;
	}
	public String getTownyTempFolder()
	{
		return getWorldRegenTempFolder()+townytempfolder;
	}
	
}
