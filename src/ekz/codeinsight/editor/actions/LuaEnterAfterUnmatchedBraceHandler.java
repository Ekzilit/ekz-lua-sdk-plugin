package ekz.codeinsight.editor.actions;

import com.intellij.codeInsight.editorActions.enter.EnterAfterUnmatchedBraceHandler;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import ekz.codeinsight.highlighting.LuaPairedBraceMatcher;
import org.jetbrains.annotations.NotNull;

public abstract class LuaEnterAfterUnmatchedBraceHandler extends EnterAfterUnmatchedBraceHandler {

  abstract String getRBrace();

  abstract IElementType getRBraceType();

  @Override
  protected int getRBraceOffset(@NotNull final PsiFile file, @NotNull final Editor editor, final int caretOffset) {
    return super.getRBraceOffset(file, editor, caretOffset);
  }

  @Override
  public Result preprocessEnter(@NotNull final PsiFile file, @NotNull final Editor editor,
                                @NotNull final Ref<Integer> caretOffsetRef, @NotNull final Ref<Integer> caretAdvance,
                                @NotNull final DataContext dataContext, final EditorActionHandler originalHandler) {

    int offset = caretOffsetRef.get();
    if (offset != 0) {
      var chars = editor.getDocument().getCharsSequence();
      var highlighter = ((EditorEx) editor).getHighlighter();
      var iterator = highlighter.createIterator(offset - 1);
      final var fileType = file.getFileType();
      var braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType, iterator);
      if (braceMatcher instanceof LuaPairedBraceMatcher) {
        var language = iterator.getTokenType().getLanguage();
/*            if (!braceMatcher.isLBraceToken(iterator, chars, fileType) || !braceMatcher.isStructuralBrace(iterator, chars,
fileType)) {
                return -1;
            }*/

        iterator = highlighter.createIterator(0);
        var lBracesBeforeOffset = 0;
        var lBracesAfterOffset = 0;
        var rBracesBeforeOffset = 0;
        var rBracesAfterOffset = 0;
        for (; !iterator.atEnd(); iterator.advance()) {
          var tokenType = iterator.getTokenType();
          if (!tokenType.getLanguage().equals(language) || !braceMatcher.isStructuralBrace(iterator, chars, fileType)) {
            continue;
          }

          var beforeOffset = iterator.getStart() < offset;

          if (((LuaPairedBraceMatcher) braceMatcher).isLBraceTokenOfRBrace(iterator, chars, fileType,
              getRBraceType())) {
            if (beforeOffset) {
              lBracesBeforeOffset++;
            } else {
              lBracesAfterOffset++;
            }
          } else if (((LuaPairedBraceMatcher) braceMatcher).isRBraceTokenOfRBrace(iterator, chars, fileType,
              getRBraceType())) {
            if (beforeOffset) {
              rBracesBeforeOffset++;
            } else {
              rBracesAfterOffset++;
            }
          }
        }

        var maxBracesToInsert = lBracesBeforeOffset - rBracesBeforeOffset - (rBracesAfterOffset - lBracesAfterOffset);
        if (maxBracesToInsert > 0) {
          insertRBraces(file, editor, offset, getRBraceOffset(file, editor, offset),
              generateStringToInsert(editor, offset, maxBracesToInsert));
          return Result.DefaultForceIndent;
        }
      } else {
        super.preprocessEnter(file, editor, caretOffsetRef, caretAdvance, dataContext, originalHandler);
      }
    }
    return Result.Continue;
  }

  @NotNull
  @Override
  protected String generateStringToInsert(@NotNull final Editor editor, final int caretOffset,
                                          final int maxRBraceCount) {
    /*        return super.generateStringToInsert(editor, caretOffset, maxRBraceCount);*/
    return StringUtil.repeat(getRBrace(), Math.max(maxRBraceCount, 1));
  }

}
