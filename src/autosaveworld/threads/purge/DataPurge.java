package autosaveworld.threads.purge;

import autosaveworld.config.AutoSaveWorldConfig;

public abstract class DataPurge {

	protected AutoSaveWorldConfig config;
	protected ActivePlayersList activeplayerslist;

	public DataPurge(AutoSaveWorldConfig config, ActivePlayersList activeplayerslist) {
		this.config = config;
		this.activeplayerslist = activeplayerslist;
	}

	public abstract void doPurge();

}
