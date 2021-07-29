package ekz.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import ekz.psi.LuaElementType;
import ekz.psi.LuaIdName;
import ekz.psi.stubs.LuaGVarStub;

public class LuaGVarStubImpl extends StubBase<LuaIdName> implements LuaGVarStub {

	public LuaGVarStubImpl(final StubElement parent) {
		super(parent, LuaElementType.LUA_GVAR);
	}
}
