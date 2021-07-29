package ekz;

import com.intellij.lang.Language;

public class Lua extends Language {
	public static final Lua INSTANCE = new Lua();

	private Lua() {
		super("Lua");
	}
}
