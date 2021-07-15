package ekz.codeinsight.editor.actions;

import com.intellij.application.options.CodeStyle;
import com.intellij.formatting.Indent;
import com.intellij.formatting.IndentImpl;
import com.intellij.formatting.IndentInfo;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.formatting.Indent.Type.CONTINUATION;
import static com.intellij.formatting.Indent.Type.NORMAL;
import static com.intellij.formatting.Indent.Type.SPACES;

public class LuaIndentCalculator {

  public static final BaseLineOffsetCalculator LINE_BEFORE = currPosition -> CharArrayUtil.shiftBackward(
      currPosition.getChars(), currPosition.getStartOffset(), " \t\n\r");
  public static final BaseLineOffsetCalculator LINE_AFTER = currPosition -> CharArrayUtil.shiftForward(
      currPosition.getChars(), currPosition.getStartOffset(), " \t\n\r");
  private @NotNull
  final Project myProject;
  private @NotNull
  final Editor myEditor;
  private @NotNull
  final BaseLineOffsetCalculator myBaseLineOffsetCalculator;
  private @NotNull
  final Indent myIndent;

  public LuaIndentCalculator(@NotNull Project project, @NotNull Editor editor,
                             @NotNull BaseLineOffsetCalculator baseLineOffsetCalculator, @NotNull Indent indent) {
    this.myProject = project;
    this.myEditor = editor;
    this.myBaseLineOffsetCalculator = baseLineOffsetCalculator;
    this.myIndent = indent;
  }

  private static int indentToSize(@NotNull Indent indent, @NotNull CommonCodeStyleSettings.IndentOptions options) {
    if (indent.getType() == NORMAL) {
      return options.INDENT_SIZE;
    } else if (indent.getType() == CONTINUATION) {
      return options.CONTINUATION_INDENT_SIZE;
    } else if (indent.getType() == SPACES && indent instanceof IndentImpl) {
      return ((IndentImpl) indent).getSpaces();
    }
    return 0;
  }

  @Nullable
  public String getIndentString(@Nullable Language language, @NotNull SemanticEditorPosition currPosition) {
    var baseIndent = getBaseIndent(currPosition);
    var document = myEditor.getDocument();
    var file = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
    if (file != null) {
      var fileOptions = CodeStyle.getIndentOptions(file);
      var options = !fileOptions.isOverrideLanguageOptions() && language != null &&
          !(language.is(file.getLanguage()) || language.is(Language.ANY)) ? CodeStyle.getLanguageSettings(file,
          language).getIndentOptions() : fileOptions;
      if (options != null) {
        return baseIndent + new IndentInfo(0, indentToSize(myIndent, options), 0, false).generateNewWhiteSpace(options);
      }
    }
    return null;
  }

  @NotNull
  protected String getBaseIndent(@NotNull SemanticEditorPosition currPosition) {
    var docChars = myEditor.getDocument().getCharsSequence();
    var offset = currPosition.getStartOffset();
    if (offset > 0) {
      var indentLineOffset = myBaseLineOffsetCalculator.getOffsetInBaseIndentLine(currPosition);
      if (indentLineOffset > 0) {
        var indentStart = CharArrayUtil.shiftBackwardUntil(docChars, indentLineOffset, "\n") + 1;
        if (indentStart >= 0) {
          var indentEnd = CharArrayUtil.shiftForward(docChars, indentStart, " \t");
          if (indentEnd > indentStart) {
            return docChars.subSequence(indentStart, indentEnd).toString();
          }
        }
      }
    }
    return "";
  }

  public interface BaseLineOffsetCalculator {
    int getOffsetInBaseIndentLine(@NotNull SemanticEditorPosition position);
  }

}
