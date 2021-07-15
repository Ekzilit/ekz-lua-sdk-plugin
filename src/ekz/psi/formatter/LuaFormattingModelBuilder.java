package ekz.psi.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.TokenSet;
import ekz.Lua;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

public class LuaFormattingModelBuilder implements FormattingModelBuilder {
  private static final TokenSet OPERATORS = TokenSet.create(LuaTypes.COMPARISON_OPERATOR, LuaTypes.NUMERIC_OPERATOR);

  @NotNull
  @Override
  public FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    var element = formattingContext.getPsiElement();
    var settings = formattingContext.getCodeStyleSettings();
    return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(),
        new LuaBlock(element.getNode(), Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment(),
            new SpacingBuilder(settings, Lua.INSTANCE).around(
                TokenSet.create(LuaTypes.ASSIGN, LuaTypes.CONCAT, LuaTypes.AND, LuaTypes.OR))
                .spaces(1)
                .around(OPERATORS)
                .spaces(1)
                .after(TokenSet.create(LuaTypes.COMMA, LuaTypes.IF, LuaTypes.FOR, LuaTypes.NOT))
                .spaces(1)
                .after(TokenSet.create(LuaTypes.LPAREN, LuaTypes.UNARY_OP))
                .spaces(0)
                .before(TokenSet.create(LuaTypes.COMMA))
                .spaces(0)
                .around(TokenSet.create(LuaTypes.DOT, LuaTypes.COLON, LuaTypes.SEMI))
                .spaces(0)
                .before(TokenSet.create(LuaTypes.THEN, LuaTypes.NIL, LuaTypes.DO))
                .spaces(1)
                .between(LuaTypes.FUNC_PARAMS, LuaTypes.FUNC_PARAMS)
                .spaces(0)
                .between(OPERATORS, LuaTypes.LPAREN)
                .spaces(1)
                .between(LuaTypes.CLASS_HEADER, LuaTypes.TABLE)
                .lineBreakOrForceSpace(false, true), settings), settings);
  }
}
