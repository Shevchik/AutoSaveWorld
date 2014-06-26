package autosaveworld.threads.purge.weregen;

import java.util.HashSet;
import java.util.Set;

public class RegenOptions {

	private boolean removeunsafeblocks = false;
	private boolean[] safelist = new boolean[4096];

	public RegenOptions() {
	}

	public RegenOptions(Set<Integer> safeblocks) {
		if (safeblocks.isEmpty()) {
			return;
		}
		removeunsafeblocks = true;
		for (int safeblockid : safeblocks) {
			safelist[safeblockid] = true;
		}
	}

	public boolean shouldRemoveUnsafeBlocks() {
		return removeunsafeblocks;
	}

	public boolean isBlockSafe(int id) {
		return safelist[id];
	}

	public static HashSet<Integer> parseListToIDs(Set<String> list) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (String element : list) {
			if (element.contains("-")) {
				try {
					String[] split = element.split("[-]");
					int start = Integer.parseInt(split[0]);
					int end = Integer.parseInt(split[1]);
					for (int i = start; i <= end; i++) {
						set.add(i);
					}
				} catch (Exception e) {
				}
			} else {
				try {
					set.add(Integer.parseInt(element));
				} catch (Exception e) {
				}
			}
		}
		return set;
	}

}
