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

package autosaveworld.utils.codeinvoker;

import java.util.HashMap;
import java.util.LinkedList;

public class CodeContext {

	protected Class<?> usedclass;
	protected Object returnedobject;
	protected HashMap<String, Object> objectsrefs = new HashMap<String, Object>();

	protected Object[] getObjects(String objectsstring) {
		LinkedList<Object> objects = new LinkedList<Object>();
		String[] split = objectsstring.split("[|]");
		for (String obj : split) {
			objects.add(parseObject(obj));
		}
		return objects.toArray();
	}

	private Object parseObject(String object) {
		String[] split = object.split("[:]");
		switch (split[0].toUpperCase()) {
			case "STRING": {
				return new String(split[1].replace("{LINEREPLACER}", "|"));
			}
			case "LONG": {
				return Long.parseLong(split[1]);
			}
			case "INTEGER": {
				return Integer.parseInt(split[1]);
			}
			case "SHORT": {
				return Short.parseShort(split[1]);
			}
			case "BYTE": {
				return Byte.parseByte(split[1]);
			}
			case "DOUBLE": {
				return Double.parseDouble(split[1]);
			}
			case "FLOAT": {
				return Float.parseFloat(split[1]);
			}
			case "CONTEXT": {
				return objectsrefs.get(split[1]);
			}
			case "NULL": {
				return null;
			}
			case "LAST": {
				return returnedobject;
			}
			default: {
				return new Object();
			}
		}
	}

}
