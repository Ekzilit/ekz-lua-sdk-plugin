package ekz.codeinsight.navigation;

import com.intellij.codeInsight.generation.actions.PresentableCodeInsightActionHandler;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassVarName;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static ekz.codeinsight.navigation.LuaClassHelper.getParentClass;

public class LuaGotoSuperHandler implements PresentableCodeInsightActionHandler {

  @Override
  public void update(@NotNull Editor editor, @NotNull PsiFile file, Presentation presentation) {
/*        final PsiElement element = getElement(file, editor.getCaretModel().getOffset());
        final PsiElement containingElement = PsiTreeUtil.getParentOfType(element, PsiFunctionalExpression.class, PsiMember.class);
        if (containingElement instanceof PsiClass) {
            presentation.setText(ActionsBundle.actionText("GotoSuperClass"));
            presentation.setDescription(ActionsBundle.actionDescription("GotoSuperClass"));
        }
        else {
            presentation.setText(ActionsBundle.actionText("GotoSuperMethod"));
            presentation.setDescription(ActionsBundle.actionDescription("GotoSuperMethod"));
        }*/

  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    var offset = editor.getCaretModel().getOffset();
    var currentElement = file.findElementAt(offset);
    var parent = PsiTreeUtil.findFirstParent(currentElement,
        p -> p instanceof LuaClass || (p instanceof LuaClassElementDefinition));
    if (Objects.nonNull(parent)) {
      if (parent instanceof LuaClass) {
        goToSuperClass(project, (LuaClass) parent);
      } else if (parent instanceof LuaClassElementDefinition) {
        goToSuperElement(project, (LuaClassElementDefinition) parent);
      }
    }
  }

  private void goToSuperElement(@NotNull final Project project, final LuaClassElementDefinition classElement) {
    var luaClass = (LuaClass) classElement.getParent().getParent();
    var parentClass = getParentClass(luaClass);
    while (Objects.nonNull(parentClass)) {
      var parentElement = parentClass.getClassBody()
          .getClassElementDefinitionList()
          .stream()
          .filter(classElementDefinition -> PsiTreeUtil.findChildOfAnyType(classElement, LuaClassVarName.class,
              LuaClassMethodName.class)
              .getText()
              .equals(PsiTreeUtil.findChildOfAnyType(classElementDefinition, LuaClassVarName.class, LuaClassMethodName.class)
                  .getText()))
          .findFirst()
          .orElse(null);
      if (Objects.nonNull(parentElement)) {
        var descriptor = PsiNavigationSupport.getInstance()
            .createNavigatable(project, parentElement.getContainingFile().getVirtualFile(), parentElement.getTextOffset());
        descriptor.navigate(true);
        break;
      }
      parentClass = getParentClass(parentClass);
    }
  }

  private void goToSuperClass(@NotNull final Project project, final LuaClass parent) {
    var parentClass = getParentClass(parent);
    if (Objects.nonNull(parentClass)) {
      var descriptor = PsiNavigationSupport.getInstance()
          .createNavigatable(project, parentClass.getContainingFile().getVirtualFile(), parentClass.getTextOffset());
      descriptor.navigate(true);
    }
  }
}
