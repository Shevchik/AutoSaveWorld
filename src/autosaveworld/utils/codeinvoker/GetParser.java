package autosaveworld.utils.codeinvoker;

public class GetParser {

	private CodeContext context;
	public GetParser(CodeContext context) {
		this.context = context;
	}

	protected GetInfo getGetInfo(String string) {
		GetInfo info = new GetInfo();
		String[] split = string.split("[,]");
		info.fieldname = split[0];
		info.object = context.getObjects(split[1])[0];
		return info;
	}

	protected static class GetInfo {
		private String fieldname;
		private Object object;
		protected String getFieldName() {
			return fieldname;
		}
		protected Object getObject() {
			return object;
		}
	}

}
