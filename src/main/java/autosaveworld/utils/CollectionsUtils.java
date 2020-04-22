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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionsUtils {

	public static <T> List<T> getSomeElements(Iterator<T> iterator, int limit) {
		ArrayList<T> list = new ArrayList<>();
		int currentAmount = 0;
		while (iterator.hasNext() && currentAmount++ < limit) {
			list.add(iterator.next());
		}
		return list;
	}

	public static <T> List<Collection<T>> split(Collection<T> collection, int limit) {
        List<Collection<T>> list = new ArrayList<>();
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
        	list.add(getSomeElements(iterator, limit));
        }
        return list;
	}

}
