package autosaveworld.features.backup;

public abstract class Backup {

	protected final String name;
	public Backup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract void performBackup() throws Exception;

}
