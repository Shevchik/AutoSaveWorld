package autosaveworld.utils.codeinvoker;

public class SetParser {

	private CodeContext context;
	public SetParser(CodeContext context) {
		this.context = context;
	}

	protected SetInfo getSetInfo(String string) {
		SetInfo info = new SetInfo();
		String[] split = string.split("[,]");
		info.fieldname = split[0];
		info.object = context.getObjects(split[1])[0];
		info.set = context.getObjects(split[2])[0];
		return info;
	}

	protected static class SetInfo {
		private String fieldname;
		private Object object;
		private Object set;
		protected String getFieldName() {
			return fieldname;
		}
		protected Object getObject() {
			return object;
		}
		protected Object getNew() {
			return set;
		}
	}

}
