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
	public String getConfigMSGWithSuffix(String suffix)
	{
		String[] cf = configmsgfile.split("[.]");
		return getPluginFolder()+cf[0]+"_"+suffix+"."+cf[1];
	}

	//backup
	private String backuptempfolder = "BackupTemp/";
	public String getBackupTempFolder()
	{
		return getPluginFolder()+backuptempfolder;
	}

	//worldregen
	private String worldregentempfolder = "WorldRegenTemp/";
	private String worldnamefile = "wname.yml";
	private String shouldpastefile = "shouldpaste";
	private String wgtempfolder = "WG/";
	private String factionstempfolder = "Factions/";	
	private String griefpreventionfolder = "GP/";
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
		return getWorldRegenTempFolder()+griefpreventionfolder;
	}
	
}
