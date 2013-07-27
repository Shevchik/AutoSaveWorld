package autosaveworld.threads.worldregen;

public class WorldRegenConstants {

	private static String tempfolder = "plugins/AutoSaveWorld/WorldRegenTemp/";
	private static String worldnamefile = "wname.yml";
	private static String shouldpastefile = "shouldpaste";

	private static String wgtempfolder = "WG/";
	private static String factionstempfolder = "Factions/";
	
	public static String getTempFolder()
	{
		return tempfolder;
	}
	public static String getWorldnameFile()
	{
		return tempfolder+worldnamefile;
	}
	public static String getShouldpasteFile()
	{
		return tempfolder+shouldpastefile;
	}
	
	public static String getWGTempFolder()
	{
		return tempfolder+wgtempfolder;
	}
	public static String getFactionsTempFolder()
	{
		return tempfolder+factionstempfolder;
	}
}
