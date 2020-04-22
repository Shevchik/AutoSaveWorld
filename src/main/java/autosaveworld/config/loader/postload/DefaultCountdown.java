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

package autosaveworld.config.loader.postload;

import java.util.Collections;
import java.util.List;

public class DefaultCountdown implements PostLoad {

	@SuppressWarnings("unchecked")
	@Override
	public void postLoad(Object value) {
		List<Integer> list = (List<Integer>) value;
		if (list.isEmpty()) {
			list.add(60);
			list.add(30);
			for (int i = 1; i <= 10; i++) {
				list.add(i);
			}
		}
		Collections.sort(list, Collections.reverseOrder());
	}

}
