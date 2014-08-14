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

public class IfParser {

	private CodeContext context;
	public IfParser(CodeContext context) {
		this.context = context;
	}

	protected IfInfo getIfInfo(String string) {
		IfInfo info = new IfInfo();
		String[] split = string.split("[,]");
		info.ife = Integer.parseInt(split[0]);
		info.ifne = Integer.parseInt(split[1]);
		info.objects = context.getObjects(split[2]);
		return info;
	}

	protected static class IfInfo {
		private int ife;
		private int ifne;
		private Object[] objects;
		protected int getEIndex() {
			return ife;
		}
		protected int getNEIndex() {
			return ifne;
		}
		protected Object[] getObjects() {
			return objects;
		}
	}

	
}
