package ekz.psi.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.util.Processor;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static ekz.psi.search.LuaClassElementImplementationsSearchHelper.getElementImplementation;
import static ekz.psi.search.LuaClassImplementationsSearchHelper.getChildren;

public class LuaClassElementImplementationsSearcher extends QueryExecutorBase<LuaClassElementDefinition, LuaClassElementImplementationsSearch.SearchParameters> {
  @Override
  public void processQuery(@NotNull LuaClassElementImplementationsSearch.SearchParameters searchParameters,
                           @NotNull Processor<? super LuaClassElementDefinition> processor) {

    DumbService.getInstance(searchParameters.getClassElementDefinition().getProject()).runReadActionInSmartMode(() -> {
      getChildren(((LuaClass) searchParameters.getClassElementDefinition().getParent().getParent()).getClassHeader()
          .getClassName()).stream()
          .map(classHeader -> getElementImplementation(searchParameters.getClassElementDefinition(), classHeader))
          .filter(Objects::nonNull)
          .forEach(processor::process);
      return true;
    });
  }
}
