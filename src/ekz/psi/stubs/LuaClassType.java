package ekz.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import ekz.Lua;
import ekz.psi.LuaClass;
import ekz.psi.LuaParentName;
import ekz.psi.impl.LuaClassImpl;
import ekz.psi.stubs.impl.LuaClassStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

public class LuaClassType extends IStubElementType<LuaClassStub, LuaClass> {
	public LuaClassType() {
		super("LUA_CLASS", Lua.INSTANCE);
	}

	@Override
	public LuaClass createPsi(@NotNull final LuaClassStub luaClassStub) {
		return new LuaClassImpl(luaClassStub, this);
	}

	@NotNull
	@Override
	public LuaClassStub createStub(@NotNull final LuaClass luaClass, final StubElement stubElement) {
		var classParent = "";
		if (Objects.nonNull(luaClass.getClassHeader().getParentName())) {
			classParent = luaClass.getClassHeader().getParentName().getUnquotedText();
		} else if (Objects.nonNull(luaClass.getClassHeader().getParentNamesList())) {
			classParent = String.join(";", luaClass.getClassHeader()
					.getParentNamesList()
					.getParentNameList()
					.stream()
					.map(LuaParentName::getUnquotedText)
					.collect(Collectors.toList()));
		} else {
			classParent = "Object";
		}
		var classPackage = luaClass.getClassPackageDefinition().getClassPackage().getUnquotedText();
/*
--TODO
possibly when get luaClass by name check it module, on indexing module is not visible
    var module = ModuleUtil.findModuleForFile(luaClass.getContainingFile());
    return new LuaClassStubImpl(stubElement, classParent, Objects.nonNull(module) ? module.getName() : "no_module");
*/
		final var classStub = new LuaClassStubImpl(stubElement);
		classStub.setClassParent(classParent);
		classStub.setClassName(luaClass.getClassHeader().getClassName().getUnquotedText());
		classStub.setClassPackage(classPackage);
		return classStub;
	}

	@NotNull
	@Override
	public String getExternalId() {
		return "lua.class";
	}

	@Override
	public void serialize(@NotNull final LuaClassStub luaClassStub,
						  @NotNull final StubOutputStream stubOutputStream) throws IOException {
		stubOutputStream.writeName(((LuaClassStubImpl) luaClassStub).getClassParent());
		stubOutputStream.writeName(((LuaClassStubImpl) luaClassStub).getClassName());
		stubOutputStream.writeName(((LuaClassStubImpl) luaClassStub).getClassPackage());
	}

	@NotNull
	@Override
	public LuaClassStub deserialize(@NotNull final StubInputStream stubInputStream,
									final StubElement stubElement) throws IOException {
		final var classStub = new LuaClassStubImpl(stubElement);
		classStub.setClassParent(stubInputStream.readNameString());
		classStub.setClassName(stubInputStream.readNameString());
		classStub.setClassPackage(stubInputStream.readNameString());
		return classStub;
	}

	@Override
	public void indexStub(@NotNull final LuaClassStub luaClassStub, @NotNull final IndexSink indexSink) {
		indexSink.occurrence(StubKeys.LUA_CLASS, ((LuaClassStubImpl) luaClassStub).getClassName());
		indexSink.occurrence(StubKeys.LUA_CLASS_PARENT, ((LuaClassStubImpl) luaClassStub).getClassParent());
		indexSink.occurrence(StubKeys.LUA_FULL_CLASS_NAME,
				((LuaClassStubImpl) luaClassStub).getClassPackage() + "." + ((LuaClassStubImpl) luaClassStub).getClassName());
	}

}
