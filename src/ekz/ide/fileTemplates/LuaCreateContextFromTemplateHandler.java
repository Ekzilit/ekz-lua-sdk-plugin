package ekz.ide.fileTemplates;

import com.intellij.ide.fileTemplates.CreateFromTemplateHandler;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import ekz.LuaFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LuaCreateContextFromTemplateHandler implements CreateFromTemplateHandler {
  @Override
  public boolean handlesTemplate(@NotNull FileTemplate template) {
    return "LuaContext".equals(template.getName());
  }

  @NotNull
  @Override
  public PsiElement createFromTemplate(@NotNull Project project, @NotNull PsiDirectory directory, String fileName,
                                       @NotNull FileTemplate template, @NotNull String templateText,
                                       @NotNull Map<String, Object> props) throws IncorrectOperationException {
    final var name = "myContext.lua";
    var psiFile = PsiFileFactory.getInstance(project).createFileFromText(name, LuaFileType.INSTANCE, templateText);
    psiFile.setName(props.get("originalFileName") + ".lua");
    return directory.add(psiFile);
  }

  @Override
  public boolean canCreate(@NotNull PsiDirectory[] dirs) {
    return false;
  }

  @Override
  public boolean isNameRequired() {
    return false;
  }

  @NotNull
  @Override
  public String getErrorMessage() {
    return "Some error in file creation happens";
  }

  @Override
  public void prepareProperties(@NotNull Map<String, Object> props) {

  }
}
