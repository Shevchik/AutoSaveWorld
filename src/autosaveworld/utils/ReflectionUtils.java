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

package autosaveworld.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static void init() {
	}

	public static Field getField(Class<?> clazz, String name) {
		do {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getName().equals(name)) {
					field.setAccessible(true);
					return field;
				}
			}
		} while ((clazz = clazz.getSuperclass()) != null);
		throw new RuntimeException("Can't find field "+name);
	}

	public static Method getMethod(Class<?> clazz, String name, int paramlength) {
		do {
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.getName().equals(name) && (method.getParameterTypes().length == paramlength)) {
					method.setAccessible(true);
					return method;
				}
			}
		} while ((clazz = clazz.getSuperclass()) != null);
		throw new RuntimeException("Can't find method "+name+" with params length "+paramlength);
	}

	public static void throwException(Throwable exception) {
		ReflectionUtils.<RuntimeException>throwException0(exception);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwException0(Throwable exception) throws T {
		throw (T) exception;
	}

}
