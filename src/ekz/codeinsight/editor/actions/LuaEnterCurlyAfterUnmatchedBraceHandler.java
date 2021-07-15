package ekz.codeinsight.editor.actions;

import com.intellij.psi.tree.IElementType;
import ekz.psi.LuaTypes;

public class LuaEnterCurlyAfterUnmatchedBraceHandler extends LuaEnterAfterUnmatchedBraceHandler {
  private static final IElementType RBRACE_TYPE = LuaTypes.RCURLY;
  private static final String RBRACE = "}";

  @Override
  String getRBrace() {
    return RBRACE;
  }

  @Override
  IElementType getRBraceType() {
    return RBRACE_TYPE;
  }
}
