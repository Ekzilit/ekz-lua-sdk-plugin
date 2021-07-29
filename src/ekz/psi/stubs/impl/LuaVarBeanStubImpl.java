package ekz.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.LuaElementType;
import ekz.psi.stubs.LuaVarBeanStub;

public class LuaVarBeanStubImpl extends StubBase<LuaClassVarDefinition> implements LuaVarBeanStub {

	public LuaVarBeanStubImpl(final StubElement parent) {
		super(parent, LuaElementType.LUA_VAR_BEAN);
	}
}
