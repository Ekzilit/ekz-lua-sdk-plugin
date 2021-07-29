package ekz.psi.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.stubs.StubKeys;
import org.jetbrains.annotations.NotNull;

public class LuaVarBeanIndex extends StringStubIndexExtension<LuaClassVarDefinition> {
	public static final LuaVarBeanIndex INSTANCE = new LuaVarBeanIndex();

	@NotNull
	@Override
	public StubIndexKey<String, LuaClassVarDefinition> getKey() {
		return StubKeys.LUA_VAR_BEAN;
	}

	@Override
	public int getVersion() {
		return 1;
	}

}