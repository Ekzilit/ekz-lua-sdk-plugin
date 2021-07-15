package ekz.psi.stubs;

import com.intellij.psi.stubs.StubIndexKey;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.LuaIdName;

public class StubKeys {
  public static final StubIndexKey<String, LuaClass> LUA_CLASS = StubIndexKey.createIndexKey("lua.class.index.key");
  public static final StubIndexKey<String, LuaClass> LUA_FULL_CLASS_NAME = StubIndexKey.createIndexKey(
      "lua.full.class.name.index.key");
  public static final StubIndexKey<String, LuaClass> LUA_CLASS_PARENT = StubIndexKey.createIndexKey("lua.class.parent.index.key");
  public static final StubIndexKey<String, LuaClassVarDefinition> LUA_VAR_BEAN = StubIndexKey.createIndexKey(
      "lua.var.bean.index.key");
  public static final StubIndexKey<String, LuaIdName> GLOBAL_VAR = StubIndexKey.createIndexKey("lua.global.var.index.key");

}
