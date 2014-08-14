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

public class ConstructParser {

	private CodeContext context;

	public ConstructParser(CodeContext context) {
		this.context = context;
	}

	protected ConstructInfo getConstructInfo(String string) {
		ConstructInfo info = new ConstructInfo();
		String[] split = string.split("[,]");
		context.getObjects(split[0]);
		if (split.length == 2) {
			info.objects = context.getObjects(split[1]);
		}
		return info;
	}

	protected static class ConstructInfo {
		private Object[] objects;

		protected Object[] getObjects() {
			return objects;
		}
	}

}
