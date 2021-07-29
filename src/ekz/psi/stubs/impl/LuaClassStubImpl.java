package ekz.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import ekz.psi.LuaClass;
import ekz.psi.LuaElementType;
import ekz.psi.stubs.LuaClassStub;

public class LuaClassStubImpl extends StubBase<LuaClass> implements LuaClassStub {
	private String classParent;
	private String className;
	private String classPackage;

	public LuaClassStubImpl(final StubElement parent) {
		super(parent, LuaElementType.LUA_CLASS);
	}

	public String getClassParent() {
		return classParent;
	}

	public void setClassParent(String classParent) {
		this.classParent = classParent;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassPackage() {
		return classPackage;
	}

	public void setClassPackage(String classPackage) {
		this.classPackage = classPackage;
	}
}
