package autosaveworld.threads.purge;

public class UniquePlayerIdentifierDetector {
	
	public static UniquePlayerIdentifierType getUniquePlayerIdentifierType() {
		return UniquePlayerIdentifierType.NAME;
	}

	public static enum UniquePlayerIdentifierType {
		NAME, UUID;
	}

}
