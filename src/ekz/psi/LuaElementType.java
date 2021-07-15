package ekz.psi;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import ekz.Lua;
import ekz.psi.stubs.LuaClassType;
import ekz.psi.stubs.LuaGVarType;
import ekz.psi.stubs.LuaVarBeanType;
import org.jetbrains.annotations.NotNull;

public class LuaElementType extends IElementType {
  public static IStubElementType LUA_CLASS = new LuaClassType();
  public static IStubElementType LUA_GVAR = new LuaGVarType();
  public static IStubElementType LUA_VAR_BEAN = new LuaVarBeanType();

  public LuaElementType(@NotNull final String debugName) {
    super(debugName, Lua.INSTANCE);
  }
}
