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
