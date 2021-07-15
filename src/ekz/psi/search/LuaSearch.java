package ekz.psi.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.DefinitionsScopedSearch.SearchParameters;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassVarName;
import org.jetbrains.annotations.NotNull;

public class LuaSearch implements QueryExecutor<PsiElement, SearchParameters> {
  @Override
  public boolean execute(@NotNull SearchParameters searchParameters, @NotNull Processor<? super PsiElement> processor) {
    final var element = searchParameters.getElement();
    if (element instanceof LuaClassName) {
      LuaClassImplementationsSearch.search(new LuaClassImplementationsSearch.SearchParameters((LuaClassName) element))
          .forEach(processor);
    } else if (element instanceof LuaClassHeader) {
      LuaClassImplementationsSearch.search(
          new LuaClassImplementationsSearch.SearchParameters(((LuaClassHeader) element).getClassName())).forEach(processor);
    } else if (element instanceof LuaClassVarName || element instanceof LuaClassMethodName) {
      LuaClassElementImplementationsSearch.search(new LuaClassElementImplementationsSearch.SearchParameters(
          PsiTreeUtil.getParentOfType(element, LuaClassElementDefinition.class))).forEach(processor);
    }
    return true;
  }
}
