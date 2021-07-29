package ekz.psi.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import ekz.psi.LuaIdName;
import ekz.psi.stubs.StubKeys;
import org.jetbrains.annotations.NotNull;

public class LuaGVarIndex extends StringStubIndexExtension<LuaIdName> {
	public static final LuaGVarIndex INSTANCE = new LuaGVarIndex();

	@NotNull
	@Override
	public StubIndexKey<String, LuaIdName> getKey() {
		return StubKeys.GLOBAL_VAR;
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
