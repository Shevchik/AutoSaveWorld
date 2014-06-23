package autosaveworld.zlibs.com.dropbox.core.util;

import java.util.Arrays;

public class LangUtil {

	public static RuntimeException mkAssert(String messagePrefix, Throwable cause) {
		RuntimeException ae = new RuntimeException(messagePrefix + ": " + cause.getMessage());
		ae.initCause(cause);
		return ae;
	}

	public static <T> T[] arrayConcat(T[] a, T[] b) {
		if (a == null) {
			throw new IllegalArgumentException("'a' can't be null");
		}
		if (b == null) {
			throw new IllegalArgumentException("'b' can't be null");
		}
		T[] r = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, r, a.length, b.length);
		return r;
	}

}
