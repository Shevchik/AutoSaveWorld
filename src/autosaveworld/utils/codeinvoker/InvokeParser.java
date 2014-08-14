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

public class InvokeParser {

	private CodeContext context;
	public InvokeParser(CodeContext context) {
		this.context = context;
	}

	protected InvokeInfo getInvokeInfo(String string) {
		InvokeInfo info = new InvokeInfo();
		String[] split = string.split("[,]");
		info.methodname = split[0];
		info.returntype = split[1].equals("{IDM}") ? null : (Class<?>) context.getObjects(split[1])[0];
		info.object = context.getObjects(split[2])[0];
		if (split.length == 4) {
			info.objects = context.getObjects(split[3]);
		}
		return info;
	}

	protected static class InvokeInfo {
		private String methodname;
		private Class<?> returntype;
		private Object object;
		private Object[] objects;
		protected String getMethodName() {
			return methodname;
		}
		protected Class<?> getReturnType() {
			return returntype;
		}
		protected Object getObject() {
			return object;
		}
		protected Object[] getObjects() {
			return objects;
		}
	}

}
