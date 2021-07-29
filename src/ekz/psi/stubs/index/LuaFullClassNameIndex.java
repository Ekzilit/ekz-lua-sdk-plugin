package ekz.psi.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import ekz.psi.LuaClass;
import ekz.psi.stubs.StubKeys;
import org.jetbrains.annotations.NotNull;

public class LuaFullClassNameIndex extends StringStubIndexExtension<LuaClass> {
	public static final LuaFullClassNameIndex INSTANCE = new LuaFullClassNameIndex();

	@NotNull
	@Override
	public StubIndexKey<String, LuaClass> getKey() {
		return StubKeys.LUA_FULL_CLASS_NAME;
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
