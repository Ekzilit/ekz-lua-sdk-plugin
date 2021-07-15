package ekz;

import com.intellij.lexer.FlexAdapter;

public class LuaLexerAdapter extends FlexAdapter {
  public LuaLexerAdapter() {
    super(new _LuaLexer(null));
  }
}
