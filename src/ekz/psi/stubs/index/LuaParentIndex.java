package ekz.psi.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import ekz.psi.LuaClass;
import ekz.psi.stubs.StubKeys;
import org.jetbrains.annotations.NotNull;

public class LuaParentIndex extends StringStubIndexExtension<LuaClass> {
	public static final LuaParentIndex INSTANCE = new LuaParentIndex();

	@NotNull
	@Override
	public StubIndexKey<String, LuaClass> getKey() {
		return StubKeys.LUA_CLASS_PARENT;
	}

	@Override
	public int getVersion() {
		return 1;
	}

}
