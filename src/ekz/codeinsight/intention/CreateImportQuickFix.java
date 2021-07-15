package ekz.codeinsight.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import ekz.psi.codestyle.LuaImportHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateImportQuickFix extends BaseElementAtCaretIntentionAction {

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return "Imports";
  }

  @NotNull
  @Override
  public String getText() {
    return "Create import";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    return true;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    var luaImportHelper = ServiceManager.getService(LuaImportHelper.class);
    ApplicationManager.getApplication()
        .invokeLater(() -> luaImportHelper.addImport(element.getContainingFile(), editor, element.getText()));
  }

}
