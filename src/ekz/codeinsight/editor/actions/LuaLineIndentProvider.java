package ekz.codeinsight.editor.actions;

import com.intellij.formatting.Indent;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider;
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition;
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition.SyntaxElement;
import com.intellij.psi.tree.IElementType;
import ekz.Lua;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ArrayClosingBracket;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ArrayOpeningBracket;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.BlockClosingBrace;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.BlockOpeningBrace;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.Colon;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.Comma;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.DoKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ElseIfKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ElseKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.EndKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ForKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.FunctionKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.IfKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.LeftParenthesis;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.RepeatKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.RightParenthesis;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.Semicolon;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.ThenKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.UntilKeyword;
import static ekz.codeinsight.editor.actions.LuaLineIndentProvider.LuaLikeElement.Whitespace;

public class LuaLineIndentProvider implements LineIndentProvider {
  private static final HashMap<IElementType, SyntaxElement> SYNTAX_MAP = new HashMap<>();

  static {
    SYNTAX_MAP.put(TokenType.WHITE_SPACE, Whitespace);
    SYNTAX_MAP.put(LuaTypes.SEMI, Semicolon);
    SYNTAX_MAP.put(LuaTypes.LCURLY, BlockOpeningBrace);
    SYNTAX_MAP.put(LuaTypes.RCURLY, BlockClosingBrace);
    SYNTAX_MAP.put(LuaTypes.LBRACK, ArrayOpeningBracket);
    SYNTAX_MAP.put(LuaTypes.RBRACK, ArrayClosingBracket);
    SYNTAX_MAP.put(LuaTypes.RPAREN, RightParenthesis);
    SYNTAX_MAP.put(LuaTypes.LPAREN, LeftParenthesis);
    SYNTAX_MAP.put(LuaTypes.COLON, Colon);
    SYNTAX_MAP.put(LuaTypes.IF, IfKeyword);
    SYNTAX_MAP.put(LuaTypes.THEN, ThenKeyword);
    SYNTAX_MAP.put(LuaTypes.WHILE, IfKeyword);
    SYNTAX_MAP.put(LuaTypes.ELSE, ElseKeyword);
    SYNTAX_MAP.put(LuaTypes.ELSEIF, ElseIfKeyword);
    SYNTAX_MAP.put(LuaTypes.FOR, ForKeyword);
    SYNTAX_MAP.put(LuaTypes.REPEAT, RepeatKeyword);
    SYNTAX_MAP.put(LuaTypes.UNTIL, UntilKeyword);
    SYNTAX_MAP.put(LuaTypes.FUNC, FunctionKeyword);
    SYNTAX_MAP.put(LuaTypes.DO, DoKeyword);
    SYNTAX_MAP.put(LuaTypes.END, EndKeyword);
    SYNTAX_MAP.put(LuaTypes.COMMA, Comma);
    /*        SYNTAX_MAP.put(JavaTokenType.END_OF_LINE_COMMENT, LineComment);*/
  }

  @Nullable
  @Override
  public String getLineIndent(@NotNull Project project, @NotNull Editor editor, @Nullable Language language,
                              int offset) {
    //enter handling

        /*         position.findLeftParenthesisBackwardsSkippingNestedWithPredicate(
              LeftParenthesis,
              RightParenthesis,
              self -> self.isAtAnyOf(BlockClosingBrace, BlockOpeningBrace, Semicolon)).isAt(LeftParenthesis)*/
    /* return myFactory.createIndentCalculator(NONE, position -> position.findStartOf(BlockComment));*/

    var baseLineOffsetCalculator = LuaIndentCalculator.LINE_AFTER;
    var indent = Indent.getNoneIndent();
    if (getPosition(editor, offset).before()
        .isAtAnyOf(BlockOpeningBrace, RepeatKeyword, ThenKeyword, DoKeyword, ElseKeyword)) {
      baseLineOffsetCalculator = LuaIndentCalculator.LINE_BEFORE;
      indent = Indent.getNormalIndent();
    } else if (getPosition(editor, offset).before().isAt(EndKeyword)) {
      baseLineOffsetCalculator = LuaIndentCalculator.LINE_BEFORE;
    } else if (getPosition(editor, offset).after().isAtAnyOf(EndKeyword, UntilKeyword, ElseKeyword, ElseIfKeyword)) {
      indent = Indent.getNormalIndent();
    } else if (getPosition(editor, offset).before().isAt(RightParenthesis) &&
        isBeforeFunctionAttributes(editor, offset)) {
      indent = Indent.getNormalIndent();
      baseLineOffsetCalculator = LuaIndentCalculator.LINE_BEFORE;
    } else if (getPosition(editor, offset).after().isAtAnyOf(BlockClosingBrace)) {
      indent = Indent.getNormalIndent();
    }
    var indentCalculator = new LuaIndentCalculator(project, editor, baseLineOffsetCalculator, indent);
    return indentCalculator.getIndentString(language, getPosition(editor, offset - 1));
    /*        getPosition(editor, offset).before().before().before().before().myIterator.getTokenType()*/
  }

  private boolean isBeforeFunctionAttributes(@NotNull final Editor editor, final int offset) {
    final var position = getPosition(editor, offset);
    position.moveBeforeParentheses(LeftParenthesis, RightParenthesis);
    return position.isAt(FunctionKeyword);
  }

  @Override
  public boolean isSuitableFor(@Nullable Language language) {
    return language.isKindOf(Lua.INSTANCE);
  }

  public SemanticEditorPosition getPosition(@NotNull Editor editor, int offset) {
    return SemanticEditorPosition.createEditorPosition((EditorEx) editor, offset, this::getIteratorAtPosition,
        this::mapType);
  }

  @NotNull
  protected HighlighterIterator getIteratorAtPosition(@NotNull EditorEx editor, int offset) {
    return editor.getHighlighter().createIterator(offset);
  }

  @Nullable
  protected SyntaxElement mapType(@NotNull IElementType tokenType) {
    return SYNTAX_MAP.get(tokenType);
  }

  public enum LuaLikeElement implements SyntaxElement {
    Whitespace, Semicolon, BlockOpeningBrace, BlockClosingBrace, ArrayOpeningBracket, ArrayClosingBracket, RightParenthesis, LeftParenthesis, Colon, ElseIfKeyword, ElseKeyword, IfKeyword, ThenKeyword, ForKeyword, RepeatKeyword, UntilKeyword, DoKeyword, Comma, EndKeyword, FunctionKeyword
  }
}
