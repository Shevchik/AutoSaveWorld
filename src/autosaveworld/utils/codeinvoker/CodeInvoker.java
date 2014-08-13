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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import autosaveworld.utils.codeinvoker.ConstructParser.ConstructInfo;
import autosaveworld.utils.codeinvoker.InvokeParser.InvokeInfo;

public class CodeInvoker {

	private CodeContext context = new CodeContext();
	private InvokeParser iparser = new InvokeParser(context);
	private ConstructParser cparser = new ConstructParser(context);

	//array of objects - (String:something - string, Integer:something - integer, and so on for primitives, Context:name - codeContext object, Null - for static use in working class), separated by |, to use | character use {LINEREPLACER}

	//getclass classname - sets working class
	//construct name,objects - constructs object
	//invoke name,methodname,object,objects - invokes method
	//print objects
	//where:
	//name - codeContext variable name
	//methodname - methodname
	//object - object to which invoke on
	//objects - array of objects

	//example script
	//(print server online mode)
	//code:
	//	- getclass org.bukkit.Bukkit
	//	- invoke mode,getOnlineMode,Null
	//	- print Context:mode
	//(print offline player _Shevchik_ name)
	//code:
	//	- getclass org.bukkit.Bukkit
	//	- invoke player,getOfflinePlayer,Null,String:_Shevchik_
	//	- invoke name,getName,Context:player
	//	- print Context:name

	public void invokeCode(List<String> commands) {
		try {
			for (String command : commands) {
				String[] split = command.split("\\s+");
				CodeCommand codecommand = CodeCommand.valueOf(split[0].toUpperCase());
				switch (codecommand) {
					case GETCLASS : {
						context.usedclass = Class.forName(split[1]);
						continue;
					}
					case CONSTRUCT : {
						ConstructInfo cinfo = cparser.getConstructInfo(split[1]);
						for (Constructor<?> constr : context.usedclass.getDeclaredConstructors()) {
							boolean found = true;
							for (int i = 0; i < constr.getParameterTypes().length; i++) {
								if (constr.getParameterTypes()[i] != cinfo.getObjects()[i].getClass()) {
									found = false;
								}
							}
							if (!found) {
								continue;
							}
							constr.setAccessible(true);
							Object obj;
							if (cinfo.getObjects() == null) {
								obj = constr.newInstance();
							} else {
								obj = constr.newInstance(cinfo.getObjects());
							}
							context.objectsrefs.put(cinfo.getName(), obj);
						}
						continue;
					}
					case INVOKE: {
						InvokeInfo iinfo = iparser.getInvokeInfo(split[1]);
						for (Method method : getAllMethods(iinfo.getObject() == null ? context.usedclass : iinfo.getObject().getClass())) {
							if (!method.getName().equals(iinfo.getMethodName())) {
								continue;
							}
							boolean found = true;
							for (int i = 0; i < method.getParameterTypes().length; i++) {
								if (method.getParameterTypes()[i] != iinfo.getObjects()[i].getClass()) {
									found = false;
								}
							}
							if (!found) {
								continue;
							}
							method.setAccessible(true);
							Object obj;
							if (iinfo.getObjects() == null) {
								obj = method.invoke(iinfo.getObject());
							} else {
								obj = method.invoke(iinfo.getObject(), iinfo.getObjects());
							}
							if (method.getReturnType() != void.class) {
								context.objectsrefs.put(iinfo.getName(), obj);
							}
							break;
						}
						continue;
					}
					case PRINT: {
						for (Object obj : context.getObjects(split[1])) {
							System.out.println(obj);
						}
						continue;
					}
					default: {
						break;
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		context.objectsrefs.clear();
	}

	private Method[] getAllMethods(Class<?> clazz) {
		LinkedList<Method> methods = new LinkedList<Method>();
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		methods.addAll(Arrays.asList(clazz.getMethods()));
		return methods.toArray(new Method[methods.size()]);
	}

	private enum CodeCommand {
		GETCLASS, CONSTRUCT, INVOKE, GETFIELD, SETFIELD, PRINT
	}

}
