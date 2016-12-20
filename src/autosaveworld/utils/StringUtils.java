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

package autosaveworld.utils;

import java.util.Collection;

public class StringUtils {

	public static void init() {
	}

	public static String join(Collection<String> args, String join) {
		return join(args.toArray(new String[0]), join);
	}

	public static String join(String[] args, String join) {
		if (args.length == 0) {
			return "";
		}
		if (args.length == 1) {
			return args[0];
		}
		StringBuilder sb = new StringBuilder(50);
		sb.append(args[0]);
		for (int i = 1; i < args.length; i++) {
			sb.append(join);
			sb.append(args[i]);
		}
		return sb.toString();
	}

	public static String eraseRight(String str, int eraseLength) {
		return str.substring(0, str.length() - eraseLength);
	}

	public static boolean isNullOrEmpty(String str) {
		return (str == null) || str.isEmpty();
	}

}
