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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import autosaveworld.utils.codeinvoker.ConstructParser.ConstructInfo;
import autosaveworld.utils.codeinvoker.GetParser.GetInfo;
import autosaveworld.utils.codeinvoker.InvokeParser.InvokeInfo;
import autosaveworld.utils.codeinvoker.SetParser.SetInfo;

public class CodeInvoker {

	private CodeContext context = new CodeContext();
	private InvokeParser iparser = new InvokeParser(context);
	private ConstructParser cparser = new ConstructParser(context);
	private GetParser gparser = new GetParser(context);
	private SetParser sparser = new SetParser(context);

	//array of objects - (String:something - string, Integer:something - integer, and so on for primitives, Last - last returned object, Context:name - codeContext object, Null - for static use in working class), separated by |, to use | character use {LINEREPLACER}

	//getclass classname - sets working class
	//store name - stores last returned object
	//construct params - constructs object
	//invoke methodname,object,params - invokes method
	//get fieldname,object - gets field value
	//set fieldname,object,params - sets field value (only first object from params is used);
	//print params
	//where:
	//name - codeContext variable name
	//methodname - methodname
	//fieldname - fieldname
	//object - object to apply function
	//params - array of objects

	//example script
	//(print server online mode)
	//code:
	//	- getclass org.bukkit.Bukkit
	//	- invoke getOnlineMode,Null
	//	- print Last
	//(print offline player _Shevchik_ uuid)
	//code:
	//	- getclass org.bukkit.Bukkit
	//	- invoke getOfflinePlayer,Null,String:_Shevchik_
	//	- invoke getUniqueId,Last
	//	- print Last
	//(change server max players count to 201)
	//code:
	//	- getclass org.bukkit.Bukkit
	//	- invoke getServer,Null
	//	- get playerList,Last
	//	- set maxPlayers,Last,Integer:201

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
					case STORE: {
						context.objectsrefs.put(split[1], context.returnedobject);
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
							context.returnedobject = obj;
						}
						continue;
					}
					case INVOKE: {
						InvokeInfo iinfo = iparser.getInvokeInfo(split[1]);
						Method method = findMethod(getAllMethods(iinfo.getObject() == null ? context.usedclass : iinfo.getObject().getClass()), iinfo.getMethodName(), iinfo.getObjects());
						method.setAccessible(true);
						Object obj;
						if (iinfo.getObjects() == null) {
							obj = method.invoke(iinfo.getObject());
						} else {
							obj = method.invoke(iinfo.getObject(), iinfo.getObjects());
						}
						if (method.getReturnType() != void.class) {
							context.returnedobject = obj;
						}
						continue;
					}
					case GET: {
						GetInfo ginfo = gparser.getGetInfo(split[1]);
						Field field = findField(getAllFields(ginfo.getObject() == null ? context.usedclass : ginfo.getObject().getClass()), ginfo.getFieldName());
						field.setAccessible(true);
						context.returnedobject = field.get(ginfo.getObject());
						continue;
					}
					case SET: {
						SetInfo sinfo = sparser.getSetInfo(split[1]);
						Field field = findField(getAllFields(sinfo.getObject() == null ? context.usedclass : sinfo.getObject().getClass()), sinfo.getFieldName());
						field.setAccessible(true);
						field.set(sinfo.getObject(), sinfo.getNew());
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

	private Method findMethod(Method[] allmethods, String methodname, Object[] params) {
		for (Method method : allmethods) {
			if (!method.getName().equals(methodname)) {
				continue;
			}
			boolean found = true;
			for (int i = 0; i < method.getParameterTypes().length; i++) {
				if (method.getParameterTypes()[i] != params[i].getClass()) {
					found = false;
				}
			}
			if (!found) {
				continue;
			}
			return method;
		}
		return null;
	}

	private Method[] getAllMethods(Class<?> clazz) {
		LinkedList<Method> methods = new LinkedList<Method>();
		do {
			methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		} while ((clazz = clazz.getSuperclass()) != null);
		return methods.toArray(new Method[methods.size()]);
	}

	private Field findField(Field[] allfields, String fieldname) {
		for (Field field : allfields) {
			if (field.getName().equals(fieldname)) {
				return field;
			}
		}
		return null;
	}

	private Field[] getAllFields(Class<?> clazz) {
		LinkedList<Field> fields = new LinkedList<Field>();
		do {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		} while ((clazz = clazz.getSuperclass()) != null);
		return fields.toArray(new Field[fields.size()]);
	}

	private enum CodeCommand {
		GETCLASS, STORE, CONSTRUCT, INVOKE, GET, SET, PRINT
	}

}
