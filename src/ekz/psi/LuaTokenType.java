package ekz.psi;

import com.intellij.psi.tree.IElementType;
import ekz.Lua;
import org.jetbrains.annotations.NotNull;

public class LuaTokenType extends IElementType {
	public LuaTokenType(@NotNull final String debugName) {
		super(debugName, Lua.INSTANCE);
	}

	@Override
	public String toString() {
		return "LuaTokenType." + super.toString();
	}
}
