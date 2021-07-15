package ekz.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import ekz.LuaLexerAdapter;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LuaSyntaxHighlighter extends SyntaxHighlighterBase {
  public static final TextAttributesKey OPERATORS = DefaultLanguageHighlighterColors.OPERATION_SIGN;
  public static final TextAttributesKey KEY = DefaultLanguageHighlighterColors.KEYWORD;
  public static final TextAttributesKey STRING = DefaultLanguageHighlighterColors.STRING;
  public static final TextAttributesKey NUMBER = DefaultLanguageHighlighterColors.NUMBER;
  public static final TextAttributesKey GLOBAL_VAR = DefaultLanguageHighlighterColors.GLOBAL_VARIABLE;
  public static final TextAttributesKey LOCAL_VAR = DefaultLanguageHighlighterColors.LOCAL_VARIABLE;
  public static final TextAttributesKey FUNC_PARAMS = DefaultLanguageHighlighterColors.PARAMETER;
  public static final TextAttributesKey KEYWORD = DefaultLanguageHighlighterColors.KEYWORD;
  public static final TextAttributesKey LINE_COMMENT = DefaultLanguageHighlighterColors.LINE_COMMENT;
  public static final TextAttributesKey BLOCK_COMMENT = DefaultLanguageHighlighterColors.BLOCK_COMMENT;

  private static final List<IElementType> KEY_ELEMENTS = new ArrayList<>();

  public LuaSyntaxHighlighter() {
    super();
    KEY_ELEMENTS.add(LuaTypes.FOR);
    KEY_ELEMENTS.add(LuaTypes.DO);
    KEY_ELEMENTS.add(LuaTypes.END);
    KEY_ELEMENTS.add(LuaTypes.IF);
    KEY_ELEMENTS.add(LuaTypes.THEN);
    KEY_ELEMENTS.add(LuaTypes.WHILE);
    KEY_ELEMENTS.add(LuaTypes.REPEAT);
    KEY_ELEMENTS.add(LuaTypes.UNTIL);
    KEY_ELEMENTS.add(LuaTypes.IN);
    KEY_ELEMENTS.add(LuaTypes.NIL);
    KEY_ELEMENTS.add(LuaTypes.LOCAL);
    KEY_ELEMENTS.add(LuaTypes.FUNC);
    KEY_ELEMENTS.add(LuaTypes.ELSEIF);
    KEY_ELEMENTS.add(LuaTypes.ELSE);
    KEY_ELEMENTS.add(LuaTypes.EVAR);
    KEY_ELEMENTS.add(LuaTypes.ELLIPSIS);
    KEY_ELEMENTS.add(LuaTypes.CONCAT);
    KEY_ELEMENTS.add(LuaTypes.OR);
    KEY_ELEMENTS.add(LuaTypes.AND);
    KEY_ELEMENTS.add(LuaTypes.RETURN);
    KEY_ELEMENTS.add(LuaTypes.TRUE);
    KEY_ELEMENTS.add(LuaTypes.FALSE);
    KEY_ELEMENTS.add(LuaTypes.BREAK);
    KEY_ELEMENTS.add(LuaTypes.NOT);
    KEY_ELEMENTS.add(LuaTypes.GETN);
  }

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new LuaLexerAdapter();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(@NotNull final IElementType tokenType) {
    if (tokenType.equals(LuaTypes.ASSIGN)) {
      return new TextAttributesKey[]{OPERATORS};
    } else if (KEY_ELEMENTS.contains(tokenType)) {
      return new TextAttributesKey[]{KEY};
    } else if (tokenType.equals(LuaTypes.STRING)) {
      return new TextAttributesKey[]{STRING};
    } else if (tokenType.equals(LuaTypes.NUMBER)) {
      return new TextAttributesKey[]{NUMBER};
    } else if (tokenType.equals(LuaTypes.SHORT_COMMENT)) {
      return new TextAttributesKey[]{LINE_COMMENT};
    } else if (tokenType.equals(LuaTypes.BLOCK_COMMENT)) {
      return new TextAttributesKey[]{BLOCK_COMMENT};
    } else {
      return new TextAttributesKey[0];
    }
  }

}
