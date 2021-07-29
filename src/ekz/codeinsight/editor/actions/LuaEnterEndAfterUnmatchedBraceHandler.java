package ekz.codeinsight.editor.actions;

import com.intellij.psi.tree.IElementType;
import ekz.psi.LuaTypes;

public class LuaEnterEndAfterUnmatchedBraceHandler extends LuaEnterAfterUnmatchedBraceHandler {
	private static final IElementType RBRACE_TYPE = LuaTypes.END;
	private static final String RBRACE = "end";

	@Override
	String getRBrace() {
		return RBRACE;
	}

	@Override
	IElementType getRBraceType() {
		return RBRACE_TYPE;
	}
}
