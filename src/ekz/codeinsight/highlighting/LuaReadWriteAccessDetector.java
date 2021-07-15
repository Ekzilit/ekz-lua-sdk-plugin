package ekz.codeinsight.highlighting;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import ekz.psi.LuaFuncDefName;
import ekz.psi.LuaLocalVarAssignment;
import ekz.psi.LuaVarAssignment;
import ekz.psi.LuaVarId;
import org.jetbrains.annotations.NotNull;

public class LuaReadWriteAccessDetector extends ReadWriteAccessDetector {
  @Override
  public boolean isReadWriteAccessible(@NotNull PsiElement element) {
    return true;
  }

  @Override
  public boolean isDeclarationWriteAccess(@NotNull PsiElement element) {
    return getExpressionAccess(element) == Access.Write;
  }

  @NotNull
  @Override
  public Access getReferenceAccess(@NotNull PsiElement referencedElement, @NotNull PsiReference reference) {
    return isDeclarationWriteAccess(referencedElement) ? Access.Write : Access.Read;
  }

  @NotNull
  @Override
  public Access getExpressionAccess(@NotNull PsiElement expression) {
    //access of found elements
    if (expression.getParent() instanceof LuaVarId && expression.getParent().getParent() instanceof LuaVarAssignment) {
      if (expression.getParent().getChildren().length > 1) {
        return Access.ReadWrite;
      } else {
        return Access.Write;
      }
    }
    if (expression.getParent() instanceof LuaLocalVarAssignment) {
      return Access.Write;
    }
    //TODO fix
/*        if (expression.getParent() instanceof LuaVarId && expression.getParent()
            .getParent() instanceof LuaSimpleVal) {
            return Access.Read;
        }*/
    if (expression.getParent() instanceof LuaFuncDefName) {
      return Access.ReadWrite;
    }
    if (expression.getParent() instanceof LuaVarId && expression.getParent().getParent() instanceof PsiFile) {
      return Access.Read;
    }
    return Access.Read;
  }
}
