package ekz.codeinsight.editor.actions;

import com.intellij.psi.tree.IElementType;
import ekz.psi.LuaTypes;

public class LuaEnterRepeatAfterUnmatchedBraceHandler extends LuaEnterAfterUnmatchedBraceHandler {
	private static final IElementType RBRACE_TYPE = LuaTypes.UNTIL;
	private static final String RBRACE = "until";

	@Override
	String getRBrace() {
		return RBRACE;
	}

	@Override
	IElementType getRBraceType() {
		return RBRACE_TYPE;
	}
}
